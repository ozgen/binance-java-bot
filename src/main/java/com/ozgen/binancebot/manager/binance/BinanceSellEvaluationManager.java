package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.configuration.properties.ScheduleConfiguration;
import com.ozgen.binancebot.model.TradeStatus;
import com.ozgen.binancebot.model.binance.AssetBalance;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.bot.SellOrder;
import com.ozgen.binancebot.model.events.InfoEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.BotOrderService;
import com.ozgen.binancebot.service.FutureTradeService;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.DateFactory;
import com.ozgen.binancebot.utils.SymbolGenerator;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.ozgen.binancebot.model.ProcessStatus.BUY;
import static com.ozgen.binancebot.model.ProcessStatus.SELL;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceSellEvaluationManager {

    private final BinanceApiManager binanceApiManager;
    private final BotConfiguration botConfiguration;
    private final BotOrderService botOrderService;
    private final FutureTradeService futureTradeService;
    private final ScheduleConfiguration scheduleConfiguration;
    private final TradingSignalService tradingSignalService;
    private final BinanceHelper binanceHelper;
    private final ApplicationEventPublisher publisher;

    // todo decide which one is more suitable to sell later...
    public void processAlgorithmSellOrders() {
        Date searchDate = getSearchDate();
        List<TradingSignal> tradingSignals = this.tradingSignalService
                .getAlgorithmUsedTradingSignalsAfterDateAndIsProcessIn(searchDate, List.of(BUY));
        if (tradingSignals.isEmpty()) {
            log.info("No trading signal has been detected.");
            return;
        }

        List<BuyOrder> buyOrders = this.botOrderService.getBuyOrders(tradingSignals);
        buyOrders.forEach(this::evaluateCoinToSell);
    }

    public void evaluateCoinToSell(BuyOrder buyOrder) {
        TickerData tickerData = null;
        List<KlineData> recentKlines = null;
        try {
            tickerData = this.binanceHelper.getTickerData(buyOrder.getSymbol());
            recentKlines = this.binanceHelper.getRecentKlines(buyOrder.getSymbol());
        } catch (Exception e) {
            log.error("Error occurred while interacting with binance api");
            throw new RuntimeException(e);
        }
        if (tickerData == null || recentKlines.isEmpty()) {
            log.warn("Not enough data for analysis.");
            return;
        }
        if (this.binanceHelper.canSellCoin(buyOrder, recentKlines, tickerData)) {
            log.info("{} of coin is available to sell", buyOrder.getSymbol());
            Double sellPrice = this.binanceHelper.calculateSellPrice(buyOrder, recentKlines, tickerData);
            List<AssetBalance> assets = this.binanceHelper.getUserAssets();
            if (assets == null || assets.isEmpty()) {
                log.error("Failed to get account snapshot for BuyOrder ID {}", buyOrder.getId());
                //todo check this case is handled or not?
                return;
            }

            String coinSymbol = SymbolGenerator.getCoinSymbol(buyOrder.getSymbol(), botConfiguration.getCurrency());
            if (coinSymbol == null) {
                log.error("Coin symbol could not be generated for BuyOrder ID {}", buyOrder.getId());
                // todo Handle empty symbol scenario
                return;
            }

            SellOrder sellOrder = this.createSellOrder(buyOrder, assets, coinSymbol, sellPrice);
            if (sellOrder != null) {
                this.sendInfoMessage(sellOrder.toString());
                log.info("Sell order created successfully for BuyOrder ID {}", buyOrder.getId());
                // Additional logic if required
            } else {
                log.warn("Sell order creation failed for BuyOrder ID {}", buyOrder.getId());
                // Handle failed sell order creation with sell error future trade schedule...
            }
        }
    }

    private SellOrder createSellOrder(BuyOrder buyOrder, List<AssetBalance> assets, String coinSymbol, Double sellPrice) {
        Double coinAmount = GenericParser.getAssetFromSymbol(assets, coinSymbol);
        TradingSignal tradingSignal = buyOrder.getTradingSignal();
        double expectedTotal = buyOrder.getCoinAmount();

        if (!this.binanceHelper.isCoinAmountWithinExpectedRange(coinAmount, expectedTotal)) {
            log.error("Coin amount not within expected range for BuyOrder ID {}", buyOrder.getId());
            // Handle or cancel requests as needed
            return null;
        }

        SellOrder sellOrder = this.initializeSellOrder(buyOrder, coinAmount, sellPrice, tradingSignal);
        SellOrder saved = this.executeSellOrder(sellOrder, tradingSignal);
        return saved;
    }

    private SellOrder initializeSellOrder(BuyOrder buyOrder, Double coinAmount, Double sellPrice, TradingSignal tradingSignal) {
        double stopLoss = GenericParser.getDouble(tradingSignal.getStopLoss()).get();
        String sellOrderSymbol = buyOrder.getSymbol();

        SellOrder sellOrder = this.botOrderService.getSellOrder(tradingSignal).orElse(null);
        if (sellOrder == null) {
            sellOrder = new SellOrder();
        }

        sellOrder.setSymbol(sellOrderSymbol);
        sellOrder.setSellPrice(sellPrice);
        sellOrder.setCoinAmount(coinAmount);
        sellOrder.setTimes(sellOrder.getTimes() + 1);
        sellOrder.setStopLoss(stopLoss);
        tradingSignal.setIsProcessed(SELL);
        sellOrder.setTradingSignal(tradingSignal);

        return sellOrder;
    }

    private SellOrder executeSellOrder(SellOrder sellOrder, TradingSignal tradingSignal) {
        try {
            this.binanceApiManager.newOrderWithStopLoss(sellOrder.getSymbol(), sellOrder.getSellPrice(), sellOrder.getCoinAmount(), sellOrder.getStopLoss());
            log.info("Sell order created successfully for symbol {}", sellOrder.getSymbol());
            return this.botOrderService.createSellOrder(sellOrder);

        } catch (Exception e) {
            log.error("Failed to create sell order for symbol {}: {}", sellOrder.getSymbol(), e.getMessage(), e);
            this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.ERROR_SELL);
            return null;
        }
    }

    private Date getSearchDate() {
        return DateFactory.getDateBeforeInMonths(this.scheduleConfiguration.getMonthBefore());
    }

    private void sendInfoMessage(String message) {
        InfoEvent infoEvent = new InfoEvent(this, message);
        this.publisher.publishEvent(infoEvent);
    }
}
