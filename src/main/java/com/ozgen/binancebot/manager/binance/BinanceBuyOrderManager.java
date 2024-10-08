package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.model.TradeStatus;
import com.ozgen.binancebot.model.TradingStrategy;
import com.ozgen.binancebot.model.binance.AssetBalance;
import com.ozgen.binancebot.model.binance.OrderResponse;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.events.InfoEvent;
import com.ozgen.binancebot.model.events.NewBuyOrderEvent;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.BotOrderService;
import com.ozgen.binancebot.service.FutureTradeService;
import com.ozgen.binancebot.utils.PriceCalculator;
import com.ozgen.binancebot.utils.SyncUtil;
import com.ozgen.binancebot.utils.parser.GenericParser;
import com.ozgen.binancebot.utils.validators.TradingSignalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ozgen.binancebot.model.ProcessStatus.BUY;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceBuyOrderManager {

    private final BinanceApiManager binanceApiManager;
    private final ApplicationEventPublisher publisher;
    private final BotConfiguration botConfiguration;
    private final FutureTradeService futureTradeService;
    private final BinanceHelper binanceHelper;
    private final BotOrderService botOrderService;

    public void processNewBuyOrderEvent(NewBuyOrderEvent event) {
        TradingSignal tradingSignal = event.getTradingSignal();
        TickerData tickerData = event.getTickerData();

        try {
            List<AssetBalance> assets = this.binanceApiManager.getUserAsset();
            if (!this.binanceHelper.hasAccountEnoughAsset(assets, tradingSignal)) {
                log.warn("Account does not have enough {} (less than {}$)", this.botConfiguration.getCurrency(), this.botConfiguration.getAmount());
                this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.INSUFFICIENT);
                return;
            }

            BuyOrder buyOrder = this.createBuyOrder(tradingSignal, tickerData);
            if (buyOrder != null) {
                this.sendBuyOrderInfoMessage(buyOrder);
                SyncUtil.pauseBetweenOperations();
                this.publishNewSellOrderEvent(buyOrder);
            }
        } catch (Exception e) {
            log.error("Error processing new buy order event for trading signal {}: {}", tradingSignal.getId(), e.getMessage(), e);
            this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.ERROR_BUY);
        }
    }

    private void publishNewSellOrderEvent(BuyOrder buyOrder) {
        if (buyOrder.getTradingSignal().getStrategy() != TradingStrategy.DEFAULT) {
            return;
        }
        NewSellOrderEvent newSellOrderEvent = new NewSellOrderEvent(this, buyOrder);
        this.publisher.publishEvent(newSellOrderEvent);
        log.info("Published NewSellOrderEvent for BuyOrder {}", buyOrder.getId());
    }

    private BuyOrder createBuyOrder(TradingSignal tradingSignal, TickerData tickerData) throws Exception {
        if (!this.isTradeSignalInRange(tradingSignal, tickerData)) {
            this.handleNotInRange(tradingSignal);
            return null;
        }

        BuyOrder buyOrder = this.initializeOrRetrieveBuyOrder(tradingSignal);
        this.populateBuyOrderDetails(buyOrder, tradingSignal, tickerData);

        if (this.createOrderInBinance(buyOrder, tradingSignal)) {
            return this.saveBuyOrder(buyOrder);
        } else {
            return null;
        }
    }

    private boolean isTradeSignalInRange(TradingSignal tradingSignal, TickerData tickerData) {
        double buyPrice = PriceCalculator.calculateCoinPriceInc(GenericParser.getDouble(tickerData.getLastPrice()).get(),
                this.botConfiguration.getPercentageInc());
        return TradingSignalValidator.isAvailableToBuy(buyPrice, tradingSignal);
    }

    private void handleNotInRange(TradingSignal tradingSignal) {
        log.info("Trade signal not in range for buying: {}", tradingSignal.getId());
        this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.NOT_IN_RANGE);
    }

    private BuyOrder initializeOrRetrieveBuyOrder(TradingSignal tradingSignal) {
        BuyOrder buyOrder = this.botOrderService.getBuyOrder(tradingSignal).orElse(null);
        return (buyOrder != null) ? buyOrder : new BuyOrder();
    }

    private void populateBuyOrderDetails(BuyOrder buyOrder, TradingSignal tradingSignal, TickerData tickerData) throws Exception {
        double buyPrice = PriceCalculator.calculateCoinPriceInc(GenericParser.getDouble(tickerData.getLastPrice()).get(), this.botConfiguration.getPercentageInc());
        double coinAmount = this.binanceHelper.calculateCoinAmount(buyPrice, tradingSignal);
        double stopLoss = GenericParser.getDouble(tradingSignal.getEntryEnd()).get();

        buyOrder.setSymbol(tradingSignal.getSymbol());
        buyOrder.setCoinAmount(coinAmount);
        buyOrder.setStopLoss(stopLoss);
        buyOrder.setBuyPrice(buyPrice);
        buyOrder.setTimes(buyOrder.getTimes() + 1);
        tradingSignal.setIsProcessed(BUY);
        buyOrder.setTradingSignal(tradingSignal);
    }

    private boolean createOrderInBinance(BuyOrder buyOrder, TradingSignal tradingSignal) {
        try {
            log.info("Creating buy order for symbol {}", buyOrder.getSymbol());
            OrderResponse orderResponse = this.binanceApiManager.newOrder(buyOrder.getSymbol(), buyOrder.getBuyPrice(), buyOrder.getCoinAmount());
            log.info("Order created successfully: {}", orderResponse);
            return true;
        } catch (Exception e) {
            log.error("Failed to create buy order for symbol {}: {}", buyOrder.getSymbol(), e.getMessage(), e);
            this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.ERROR_BUY);
            return false;
        }
    }

    private BuyOrder saveBuyOrder(BuyOrder buyOrder) {
        return this.botOrderService.createBuyOrder(buyOrder);
    }

    private void sendBuyOrderInfoMessage(BuyOrder buyOrder) {
        InfoEvent infoEvent = new InfoEvent(this, buyOrder.toString());
        this.publisher.publishEvent(infoEvent);
    }
}
