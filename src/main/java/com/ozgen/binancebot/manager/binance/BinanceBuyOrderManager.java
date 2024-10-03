package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.model.ProcessStatus;
import com.ozgen.binancebot.model.TradeStatus;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceBuyOrderManager {

    private final BinanceApiManager binanceApiManager;
    private final ApplicationEventPublisher publisher;
    private final BotConfiguration botConfiguration;
    private final FutureTradeService futureTradeService;

    private final BotOrderService botOrderService;


    public void processNewBuyOrderEvent(NewBuyOrderEvent event) {
        TradingSignal tradingSignal = event.getTradingSignal();
        TickerData tickerData = event.getTickerData();

        try {
            List<AssetBalance> assets = this.binanceApiManager.getUserAsset();
            Double btcToUsdRate = this.getBtcToUsdConversionRate();
            this.processOrder(tradingSignal, tickerData, assets, btcToUsdRate);
        } catch (Exception e) {
            log.error("Error processing new buy order event for trading signal {}: {}", tradingSignal.getId(), e.getMessage(), e);
            this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.ERROR_BUY);
        }
    }

    private void processOrder(TradingSignal tradingSignal, TickerData tickerData, List<AssetBalance> assets, Double btcToUsdRate) {
        if (!this.hasAccountEnoughBtc(assets, btcToUsdRate)) {
            log.warn("Account does not have enough {} (less than {}$)", this.botConfiguration.getCurrency(), this.botConfiguration.getAmount());
            this.futureTradeService.createFutureTrade(tradingSignal, TradeStatus.INSUFFICIENT);
            return;
        }

        BuyOrder buyOrder = this.createBuyOrder(tradingSignal, tickerData, btcToUsdRate);
        if (buyOrder != null) {
            this.sendBuyOrderInfoMessage(buyOrder);
            SyncUtil.pauseBetweenOperations();
            this.publishNewSellOrderEvent(buyOrder);
        }
    }

    private void publishNewSellOrderEvent(BuyOrder buyOrder) {
        NewSellOrderEvent newSellOrderEvent = new NewSellOrderEvent(this, buyOrder);
        this.publisher.publishEvent(newSellOrderEvent);
        log.info("Published NewSellOrderEvent for BuyOrder {}", buyOrder.getId());
    }


    private double calculateCoinAmount(double buyPrice, double btcPriceInUsd) {
        double dollarsToInvest = this.botConfiguration.getAmount();
        float binanceFeePercentage = this.botConfiguration.getBinanceFeePercentage();

        // Calculate how much BTC you can buy with $500
        double btcAmount = dollarsToInvest / btcPriceInUsd;

        double binanceFee = btcAmount * binanceFeePercentage;


        // Calculate how much coin you can buy with that BTC amount
        double coinAmount = (btcAmount - binanceFee) / buyPrice;

        return coinAmount;
    }

    private boolean hasAccountEnoughBtc(List<AssetBalance> assets, Double btcToUsdRate) {
        Double btcAmount = GenericParser.getAssetFromSymbol(assets, this.botConfiguration.getCurrency());
        Double totalAccountValueInUsd = btcAmount * btcToUsdRate;
        return totalAccountValueInUsd >= this.botConfiguration.getAmount();
    }

    private Double getBtcToUsdConversionRate() throws Exception {
        TickerData tickerPrice24 = this.binanceApiManager.getTickerPrice24(this.botConfiguration.getCurrencyRate());
        return GenericParser.getDouble(tickerPrice24.getLastPrice()).get();
    }

    private BuyOrder createBuyOrder(TradingSignal tradingSignal, TickerData tickerData, double btcToUsdRate) {
        if (!this.isTradeSignalInRange(tradingSignal, tickerData)) {
            this.handleNotInRange(tradingSignal);
            return null;
        }

        BuyOrder buyOrder = this.initializeOrRetrieveBuyOrder(tradingSignal);
        this.populateBuyOrderDetails(buyOrder, tradingSignal, tickerData, btcToUsdRate);

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

    private void populateBuyOrderDetails(BuyOrder buyOrder, TradingSignal tradingSignal, TickerData tickerData, double btcToUsdRate) {
        double buyPrice = PriceCalculator.calculateCoinPriceInc(GenericParser.getDouble(tickerData.getLastPrice()).get(), this.botConfiguration.getPercentageInc());
        double coinAmount = this.calculateCoinAmount(buyPrice, btcToUsdRate);
        double stopLoss = GenericParser.getDouble(tradingSignal.getEntryEnd()).get();

        buyOrder.setSymbol(tradingSignal.getSymbol());
        buyOrder.setCoinAmount(coinAmount);
        buyOrder.setStopLoss(stopLoss);
        buyOrder.setBuyPrice(buyPrice);
        buyOrder.setTimes(buyOrder.getTimes() + 1);
        tradingSignal.setIsProcessed(ProcessStatus.BUY);
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
