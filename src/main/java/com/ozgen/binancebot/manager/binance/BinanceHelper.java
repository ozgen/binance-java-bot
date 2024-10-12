package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.AssetBalance;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.signal.FibonacciCalculator;
import com.ozgen.binancebot.signal.TradingSignalEvaluator;
import com.ozgen.binancebot.signal.ZigZagStrategy;
import com.ozgen.binancebot.utils.PriceCalculator;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BinanceHelper {

    private final BotConfiguration botConfiguration;
    private final BinanceApiManager binanceApiManager;
    private final TradingSignalEvaluator tradingSignalEvaluator;
    private final FibonacciCalculator fibonacciCalculator;
    private final ZigZagStrategy zigZagStrategy;


    public boolean hasAccountEnoughAsset(List<AssetBalance> assets, TradingSignal tradingSignal) throws Exception {
        if (tradingSignal.getInvestAmount() == null) {
            Double btcPriceInUsd = this.getBtcToUsdConversionRate();
            return this.hasAccountEnoughUSD(assets, btcPriceInUsd);
        } else {
            Double investAmountOfBtc = GenericParser.getDouble(tradingSignal.getInvestAmount());
            return this.hasAccountEnoughBTC(assets, investAmountOfBtc);
        }
    }


    public double calculateCoinAmount(double buyPrice, TradingSignal tradingSignal) throws Exception {
        float binanceFeePercentage = this.botConfiguration.getBinanceFeePercentage();
        double investAmountOfBtc = 0d;
        if (tradingSignal.getInvestAmount() == null) {
            double dollarsToInvest = this.botConfiguration.getAmount();
            Double btcPriceInUsd = this.getBtcToUsdConversionRate();
            // Calculate how much BTC you can buy with $500
            investAmountOfBtc = dollarsToInvest / btcPriceInUsd;
        } else {
            investAmountOfBtc = GenericParser.getDouble(tradingSignal.getInvestAmount());
        }
        double binanceFee = investAmountOfBtc * binanceFeePercentage;

        // Calculate how much coin you can buy with that BTC amount
        double coinAmount = (investAmountOfBtc - binanceFee) / buyPrice;

        return coinAmount;
    }

    public boolean isCoinPriceAvailableToSell(BuyOrder buyOrder, TickerData tickerData) {
        Double buyPrice = buyOrder.getBuyPrice();
        double sellPrice = PriceCalculator.calculateCoinPriceInc(buyPrice, this.botConfiguration.getProfitPercentage());
        Double lastPrice = GenericParser.getDouble(tickerData.getLastPrice());
        Double stopLoss = buyOrder.getStopLoss();
        return sellPrice <= lastPrice || lastPrice <= stopLoss;
    }

    public TickerData getTickerData(String symbol) {
        try {
            return this.binanceApiManager.getTickerPrice24(symbol);
        } catch (Exception e) {
            log.error("Error fetching ticker data for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Error fetching ticker data", e);
        }
    }

    public List<KlineData> getRecentKlines(String symbol) {
        try {
            return this.binanceApiManager.getListOfKlineData(symbol);
        } catch (Exception e) {
            log.error("Error fetching Kline data for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Error fetching Kline data", e);
        }
    }

    // todo write unit tests below methods...
    // Check if the coin should be sold based on Fibonacci and Harmonic patterns
    public boolean canSellCoin(BuyOrder buyOrder, List<KlineData> recentKlines, TickerData tickerData) {
        double currentPrice = GenericParser.getFormattedDouble(tickerData.getLastPrice());
        double stopLoss = buyOrder.getStopLoss();

        boolean hitStopLoss = currentPrice <= stopLoss;

        if (hitStopLoss) {
            log.info("Stop loss triggered for symbol {}. Current price: {}, Stop loss: {}", buyOrder.getSymbol(), currentPrice, stopLoss);
            return true;
        }

        if (this.tradingSignalEvaluator.checkTrend(recentKlines).equals(TrendingStatus.SELL)) {
            log.info("Fibonacci level or harmonic pattern detected for sell. Symbol: {}, Current price: {}",
                    buyOrder.getSymbol(), currentPrice);
            return true;
        }

        return false;
    }

    public Double calculateSellPrice(BuyOrder buyOrder, List<KlineData> recentKlines, TickerData tickerData) {
        double currentPrice = GenericParser.getFormattedDouble(tickerData.getLastPrice());
        double stopLoss = buyOrder.getStopLoss();

        // Convert KlineData high/low prices into a list of double values
        List<Double> highPrices = recentKlines.stream().map(k -> GenericParser.getFormattedDouble(k.getHighPrice())).toList();
        List<Double> lowPrices = recentKlines.stream().map(k -> GenericParser.getFormattedDouble(k.getLowPrice())).toList();

        // Calculate the ZigZag points for highs and lows
        List<Double> zigZagPointsHigh = this.zigZagStrategy.calculateZigZag(highPrices);
        List<Double> zigZagPointsLow = this.zigZagStrategy.calculateZigZag(lowPrices);

        // Use the last two ZigZag points for the Fibonacci calculation
        double high = zigZagPointsHigh.get(zigZagPointsHigh.size() - 1); // Last ZigZag high
        double low = zigZagPointsLow.get(zigZagPointsLow.size() - 1);    // Last ZigZag low

        // Calculate Fibonacci levels based on the ZigZag high and low points
        double[] fibLevels = this.fibonacciCalculator.calculateFibonacciLevels(high, low);

        // Initialize sell price to stopLoss
        double sellPrice = stopLoss;

        // Determine sell price based on the Fibonacci levels and current price
        if (currentPrice >= fibLevels[2]) {  // Fibonacci 0.500 level
            sellPrice = fibLevels[2];
            log.info("Setting sell price to Fibonacci 0.500 level: {}", sellPrice);
        } else if (currentPrice >= fibLevels[3]) {  // Fibonacci 0.618 level
            sellPrice = fibLevels[3];
            log.info("Setting sell price to Fibonacci 0.618 level: {}", sellPrice);
        }

        // Return the calculated sell price
        return sellPrice;
    }


    public Double calculateSellPriceWithBotConfiguration(BuyOrder buyOrder) {
        double sellPrice = PriceCalculator.calculateCoinPriceInc(buyOrder.getBuyPrice(), this.botConfiguration.getProfitPercentage());
        double stopLoss = buyOrder.getStopLoss();
        TickerData tickerData;
        try {
            tickerData = this.getTickerData(buyOrder.getSymbol());
        } catch (Exception exception) {
            tickerData = null;
        }
        if (tickerData == null) {
            return sellPrice;
        }
        Double currentPrice = GenericParser.getDouble(tickerData.getLastPrice());
        if (currentPrice <= stopLoss) {
            sellPrice = stopLoss;
        }
        return sellPrice;
    }

    public List<AssetBalance> getUserAssets() {
        try {
            return binanceApiManager.getUserAsset();
        } catch (Exception e) {
            log.error("Error fetching user assets: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isCoinAmountWithinExpectedRange(Double coinAmount, double expectedTotal) {
        if (coinAmount >= expectedTotal) {
            log.info("All buy orders completed successfully.");
            return true;
        } else if (coinAmount > 0 && coinAmount < expectedTotal) {
            log.info("Partial buy orders completed.");
            return true;
        } else {
            return false;
        }
    }

    private Double getBtcToUsdConversionRate() throws Exception {
        TickerData tickerPrice24 = this.binanceApiManager.getTickerPrice24(this.botConfiguration.getCurrencyRate());
        return GenericParser.getDouble(tickerPrice24.getLastPrice());
    }

    private boolean hasAccountEnoughUSD(List<AssetBalance> assets, Double btcToUsdRate) {
        Double btcAmount = GenericParser.getAssetFromSymbol(assets, this.botConfiguration.getCurrency());
        Double totalAccountValueInUsd = btcAmount * btcToUsdRate;
        return totalAccountValueInUsd >= this.botConfiguration.getAmount();
    }

    private boolean hasAccountEnoughBTC(List<AssetBalance> assets, Double btcInvestAmount) {
        Double btcAmount = GenericParser.getAssetFromSymbol(assets, this.botConfiguration.getCurrency());
        return btcAmount >= btcInvestAmount;
    }
}
