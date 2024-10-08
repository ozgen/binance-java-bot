package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradingSignalEvaluator {

    // Generate a TradingSignal based on the kline data
    public TradingSignal generateSignal(List<KlineData> klines, String symbol) {
        if (klines.size() < 5) {
            log.warn("Not enough data for analysis.");
            return null;
        }

        // Find the high and low from the kline data
        double high = klines.stream()
                .mapToDouble(k -> GenericParser.getFormattedDouble(k.getHighPrice()))
                .max()
                .orElse(0);
        double low = klines.stream()
                .mapToDouble(k -> GenericParser.getFormattedDouble(k.getLowPrice()))
                .min()
                .orElse(0);

        // Calculate Fibonacci levels
        double[] fibLevels = FibonacciCalculator.calculateFibonacciLevels(high, low);

        // Entry range and take profit levels
        double entryStart = fibLevels[0];  // 0.236 Fibonacci level
        double entryEnd = fibLevels[1];    // 0.382 Fibonacci level

        // Decide entry strings based on ordering
        String entryStartStr = entryStart < entryEnd ? String.valueOf(entryStart) : String.valueOf(entryEnd);
        String entryEndStr = entryStart < entryEnd ? String.valueOf(entryEnd) : String.valueOf(entryStart);

        // Filter valid take profit levels
        List<String> takeProfits = Arrays.stream(fibLevels, 2, fibLevels.length - 1)  // 0.500 to 0.764 levels
                .filter(profit -> profit > Math.min(entryStart, entryEnd))
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());

        // Stop loss at 1.000 Fibonacci level
        String stopLoss = String.valueOf(fibLevels[5]);  // 1.000 Fibonacci level

        // Get the latest kline data (most recent)
        KlineData latestKline = klines.get(klines.size() - 1);

        // Extract close, high, and low prices from the latest kline
        double closePrice = GenericParser.getFormattedDouble(latestKline.getClosePrice());
        double highPrice = GenericParser.getFormattedDouble(latestKline.getHighPrice());
        double lowPrice = GenericParser.getFormattedDouble(latestKline.getLowPrice());

        // Create and return the TradingSignal object
        return new TradingSignal(
                symbol,
                entryStartStr,
                entryEndStr,
                takeProfits,
                stopLoss,
                closePrice,
                highPrice,
                lowPrice
        );
    }

    public TrendingStatus checkTrend(List<KlineData> klines, String symbol) {
        boolean patternDetected = HarmonicPatterns.detectABCDPattern(
                klines.get(0), klines.get(1), klines.get(2), klines.get(3), klines.get(4));
        TradingSignal tradingSignal = this.generateSignal(klines, symbol);
        if(tradingSignal.isBuyEntry(patternDetected)){
            return TrendingStatus.BUY;
        } else if (tradingSignal.isSellEntry(patternDetected)) {
            return TrendingStatus.SELL;
        }
        return TrendingStatus.DEFAULT;
    }

    public TrendingStatus checkTrend(List<KlineData> klines, TradingSignal tradingSignal) {
        boolean patternDetected = HarmonicPatterns.detectABCDPattern(
                klines.get(0), klines.get(1), klines.get(2), klines.get(3), klines.get(4));
        if(tradingSignal.isBuyEntry(patternDetected)){
            return TrendingStatus.BUY;
        } else if (tradingSignal.isSellEntry(patternDetected)) {
            return TrendingStatus.SELL;
        }
        return TrendingStatus.DEFAULT;
    }
}
