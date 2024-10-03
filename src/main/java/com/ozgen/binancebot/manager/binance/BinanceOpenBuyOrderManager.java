package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.configuration.properties.ScheduleConfiguration;
import com.ozgen.binancebot.model.ProcessStatus;
import com.ozgen.binancebot.model.binance.CancelAndNewOrderResponse;
import com.ozgen.binancebot.model.binance.OrderInfo;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.BotOrderService;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.DateFactory;
import com.ozgen.binancebot.utils.PriceCalculator;
import com.ozgen.binancebot.utils.SyncUtil;
import com.ozgen.binancebot.utils.parser.GenericParser;
import com.ozgen.binancebot.utils.validators.TradingSignalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceOpenBuyOrderManager {


    private final BinanceApiManager binanceApiManager;
    private final TradingSignalService tradingSignalService;
    private final BotOrderService botOrderService;
    private final ApplicationEventPublisher publisher;
    private final ScheduleConfiguration scheduleConfiguration;
    private final BotConfiguration botConfiguration;

    public void processOpenBuyOrders() {
        Date searchDate = this.getSearchDate();
        List<TradingSignal> tradingSignals = this.tradingSignalService.getTradingSignalsAfterDateAndIsProcessIn(searchDate, List.of(ProcessStatus.SELL, ProcessStatus.BUY));

        for (TradingSignal tradingSignal : tradingSignals) {
            try {
                this.processOpenBuyOrder(tradingSignal);
                SyncUtil.pauseBetweenOperations();
            } catch (Exception e) {
                log.error("Error processing open buy order for trading signal {}: {}", tradingSignal.getId(), e.getMessage(), e);
                // Decide how to handle the error - log, alert, retry, etc.
            }
        }
    }

    private void processOpenBuyOrder(TradingSignal tradingSignal) {
        String symbol = tradingSignal.getSymbol();
        try {
            List<OrderInfo> openOrders = this.binanceApiManager.getOpenOrders(symbol);
            if(openOrders.isEmpty()) {
                return;
            }
            TickerData tickerPrice24 = this.binanceApiManager.getTickerPrice24(symbol);

            if (!TradingSignalValidator.isAvailableToBuy(tickerPrice24, tradingSignal)) {
                return;
            }

            List<BuyOrder> buyOrders = this.processOpenOrders(tradingSignal, tickerPrice24, openOrders);
            this.publishNewSellOrderEvents(buyOrders);
        } catch (Exception e) {
            log.error("Error processing open buy order for symbol {}: {}", symbol, e.getMessage(), e);
            // Handle the exception as needed
        }
    }

    private List<BuyOrder> processOpenOrders(TradingSignal tradingSignal, TickerData tickerPrice24, List<OrderInfo> openOrders) {
        return openOrders.stream()
                .map(orderInfo -> this.createCancelAndBuyOrder(tradingSignal, tickerPrice24, orderInfo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void publishNewSellOrderEvents(List<BuyOrder> buyOrders) {
        buyOrders.forEach(buyOrder -> {
            NewSellOrderEvent newSellOrderEvent = new NewSellOrderEvent(this, buyOrder);
            this.publisher.publishEvent(newSellOrderEvent);
            log.info("Published NewSellOrderEvent for BuyOrder {}", buyOrder.getId());
        });
    }


    private Date getSearchDate() {
        return DateFactory.getDateBeforeInMonths(this.scheduleConfiguration.getMonthBefore());
    }


    private BuyOrder createCancelAndBuyOrder(TradingSignal tradingSignal, TickerData tickerData, OrderInfo orderInfo) {
        String symbol = tradingSignal.getSymbol();
        BuyOrder buyOrder = this.prepareBuyOrder(tradingSignal, tickerData, orderInfo, symbol);
        BuyOrder saved = this.processCancelAndNewOrder(buyOrder, symbol, orderInfo);
        return saved;
    }

    private BuyOrder prepareBuyOrder(TradingSignal tradingSignal, TickerData tickerData, OrderInfo orderInfo, String symbol) {
        double coinAmount = GenericParser.getDouble(orderInfo.getOrigQty()).get() - GenericParser.getDouble(orderInfo.getExecutedQty()).get();
        double stopLoss = GenericParser.getDouble(tradingSignal.getEntryEnd()).get();
        double buyPrice = PriceCalculator.calculateCoinPriceInc(GenericParser.getDouble(tickerData.getLastPrice()).get(), this.botConfiguration.getPercentageInc());

        BuyOrder buyOrder = this.botOrderService.getBuyOrder(tradingSignal).orElse(null);
        if (buyOrder == null) {
            buyOrder = new BuyOrder();
        }
        this.updateBuyOrder(buyOrder, symbol, coinAmount, stopLoss, buyPrice, tradingSignal);
        return buyOrder;
    }

    private void updateBuyOrder(BuyOrder buyOrder, String symbol, double coinAmount, double stopLoss, double buyPrice, TradingSignal tradingSignal) {
        buyOrder.setSymbol(symbol);
        buyOrder.setCoinAmount(coinAmount);
        buyOrder.setStopLoss(stopLoss);
        buyOrder.setBuyPrice(buyPrice);
        buyOrder.setTimes(buyOrder.getTimes() + 1);
        tradingSignal.setIsProcessed(ProcessStatus.BUY);
        buyOrder.setTradingSignal(tradingSignal);
    }

    private BuyOrder processCancelAndNewOrder(BuyOrder buyOrder, String symbol, OrderInfo orderInfo) {
        try {
            CancelAndNewOrderResponse response = this.binanceApiManager.cancelAndNewOrderWithStopLoss(symbol, buyOrder.getBuyPrice(), buyOrder.getCoinAmount(), orderInfo.getOrderId());
            log.info("Order cancel and created successfully: {}", response);
            return this.botOrderService.createBuyOrder(buyOrder);
        } catch (Exception e) {
            log.error("Failed to cancel and create order for symbol " + symbol, e);
            return null;
        }
    }
}
