package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingSignalEvaluator {

    private final ZigZagStrategy zigZagStrategy;
    private final FibonacciCalculator fibonacciCalculator;


    public TradingSignal generateSignalWithTrendDecision(List<KlineData> klines, String symbol) {
        if (klines.size() < 5) {
            log.warn("Not enough data for analysis.");
            return null;
        }

        // Find the last two significant ZigZag points (high and low)
        double[] lastZigZagPoints = this.zigZagStrategy.getLastTwoZigZagPoints(klines);

        if (lastZigZagPoints == null || lastZigZagPoints.length < 2) {
            log.warn("Unable to detect the last two ZigZag points.");
            return null;
        }

        double zigzagHigh = lastZigZagPoints[0];
        double zigzagLow = lastZigZagPoints[1];

        // Determine trend direction based on ZigZag points
        TrendingStatus trendingStatus;
        if (zigzagHigh > zigzagLow) {
            trendingStatus = TrendingStatus.BUY;  // Upward trend
        } else {
            trendingStatus = TrendingStatus.SELL;  // Downward trend
        }

        // Calculate Fibonacci levels based on the last two ZigZag points
        double[] fibLevels = this.fibonacciCalculator.calculateFibonacciLevels(zigzagHigh, zigzagLow);

        // Entry range and take profit levels
        double entryStart = fibLevels[0];  // 0.236 Fibonacci level
        double entryEnd = fibLevels[1];    // 0.382 Fibonacci level

        // Decide entry strings based on ordering
        String entryStartStr = entryStart < entryEnd ? String.valueOf(entryStart) : String.valueOf(entryEnd);
        String entryEndStr = entryStart < entryEnd ? String.valueOf(entryEnd) : String.valueOf(entryStart);

        // Filter valid take profit levels
        List<String> takeProfits;

        switch (trendingStatus) {
            case BUY -> {
                // For buy signals, take profits are at higher Fibonacci levels
                takeProfits = Arrays.stream(fibLevels, 2, fibLevels.length - 1)  // 0.500 to 0.764 levels
                        .filter(profit -> profit > Math.min(entryStart, entryEnd))
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList());
            }
            case SELL -> {
                // For sell signals, take profits at lower targets
                takeProfits = Arrays.stream(fibLevels, 2, fibLevels.length - 1)  // 0.500 to 0.764 levels
                        .filter(profit -> profit < Math.max(entryStart, entryEnd))  // Filter lower profit targets
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList());
            }
            default -> takeProfits = List.of();  // In case of default or no clear trending status
        }

        // Stop loss at 1.000 Fibonacci level
        String stopLoss = String.valueOf(fibLevels[5]);  // 1.000 Fibonacci level

        // Get the latest kline data (most recent)
        KlineData latestKline = klines.get(klines.size() - 1);

        // Extract close, high, and low prices from the latest kline
        double closePrice = GenericParser.getFormattedDouble(latestKline.getClosePrice());
        double highPrice = GenericParser.getFormattedDouble(latestKline.getHighPrice());
        double lowPrice = GenericParser.getFormattedDouble(latestKline.getLowPrice());

        // Generate the TradingSignal based on the trend
        TradingSignal tradingSignal = new TradingSignal(
                symbol,
                entryStartStr,
                entryEndStr,
                takeProfits,
                stopLoss,
                closePrice,
                highPrice,
                lowPrice
        );

        tradingSignal.setTrendingStatus(trendingStatus);  // Set BUY or SELL status

        return tradingSignal;
    }

    public TrendingStatus checkTrend(List<KlineData> klines) {
        // Convert KlineData to a list of prices (using close prices for simplicity)
        List<Double> prices = klines.stream()
                .map(k -> GenericParser.getDouble(k.getClosePrice()))
                .collect(Collectors.toList());

        // Calculate the ZigZag points
        List<Double> zigZagPoints = this.zigZagStrategy.calculateZigZag(prices);

        if (zigZagPoints.size() < 2) {
            log.warn("Not enough ZigZag points for trend calculation.");
            return TrendingStatus.DEFAULT;
        }

        double lastZigZagPoint = zigZagPoints.get(zigZagPoints.size() - 1);
        double currentPrice = prices.get(prices.size() - 1);

        // Define a threshold (e.g., 1%) to confirm significant buy/sell signal
        double threshold = 0.01 * currentPrice;  // 1% of current price

        // Buy if the current price is significantly higher than the last ZigZag low
        if (currentPrice > lastZigZagPoint + threshold) {
            log.info("Buying at price: '{}'", currentPrice);
            return TrendingStatus.BUY;
        }
        // Sell if the current price is significantly lower than the last ZigZag high
        else if (currentPrice < lastZigZagPoint - threshold) {
            log.info("Selling at price: '{}'", currentPrice);
            return TrendingStatus.SELL;
        }

        return TrendingStatus.DEFAULT;
    }
}
