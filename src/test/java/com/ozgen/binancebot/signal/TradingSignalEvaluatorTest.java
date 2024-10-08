package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class TradingSignalEvaluatorTest {

    private TradingSignalEvaluator tradingSignalEvaluator;

    @BeforeEach
    void setUp() {
        tradingSignalEvaluator = new TradingSignalEvaluator();
    }

    @Test
    void testGenerateSignal_WithValidKlines_ShouldReturnValidTradingSignal() {
        // Setup mock Kline data
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");  // Mock high price
            kline.setLowPrice("1.0");   // Mock low price
            kline.setClosePrice(i == 4 ? "1.1" : "1.0");  // Mock close price, last kline has 1.1
            klines.add(kline);
        }

        // Generate trading signal
        TradingSignal tradingSignal = tradingSignalEvaluator.generateSignal(klines, "BTCUSDT");

        // Assert the values of the generated signal
        assertThat(tradingSignal).isNotNull();
        assertThat(tradingSignal.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(tradingSignal.getEntryStart()).isNotEmpty();
        assertThat(tradingSignal.getEntryEnd()).isNotEmpty();
        assertThat(tradingSignal.getStopLoss()).isNotEmpty();

        // Ensure the last close price is fetched properly
        assertThat(tradingSignal.getClosePrice()).isEqualTo(1.0);  // Check last close price
    }

    @Test
    void testGenerateSignal_WithInsufficientKlines_ShouldReturnNull() {
        // Setup insufficient Kline data (less than 5 klines)
        List<KlineData> klines = new ArrayList<>();
        KlineData kline = mock(KlineData.class);
        when(kline.getHighPrice()).thenReturn("1.2");
        when(kline.getLowPrice()).thenReturn("1.0");
        klines.add(kline);

        // Generate trading signal with insufficient klines
        TradingSignal tradingSignal = tradingSignalEvaluator.generateSignal(klines, "BTCUSDT");

        // Assert that the result is null
        assertThat(tradingSignal).isNull();
    }

    @Test
    void testCheckTrend_WithDetectedPattern_ShouldReturnBuyOrSell() {
        // Setup mock Kline data and TradingSignal
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");
            kline.setLowPrice("1.0");
            kline.setClosePrice("1.1");
            klines.add(kline);
        }
        TradingSignal tradingSignal = mock(TradingSignal.class);

        // Mock pattern detection using mockStatic for static methods
        try (var harmonicPatternsMock = mockStatic(HarmonicPatterns.class)) {
            harmonicPatternsMock.when(() -> HarmonicPatterns.detectABCDPattern(
                            klines.get(0), klines.get(1), klines.get(2), klines.get(3), klines.get(4)))
                    .thenReturn(true);  // Simulate pattern detection returning true

            // Mock buy and sell entry conditions
            when(tradingSignal.isBuyEntry(true)).thenReturn(true);
            when(tradingSignal.isSellEntry(true)).thenReturn(false);

            // Check the trend
            TrendingStatus trend = tradingSignalEvaluator.checkTrend(klines, tradingSignal);

            // Assert that the result is BUY
            assertThat(trend).isEqualTo(TrendingStatus.BUY);
        }
    }

    @Test
    void testCheckTrend_NoPatternDetected_ShouldReturnDefault() {
        // Setup mock Kline data and TradingSignal
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");
            kline.setLowPrice("1.0");
            kline.setClosePrice("1.1");
            klines.add(kline);
        }
        TradingSignal tradingSignal = mock(TradingSignal.class);


        when(tradingSignal.isBuyEntry(false)).thenReturn(false);
        when(tradingSignal.isSellEntry(false)).thenReturn(false);

        // Check the trend
        TrendingStatus trend = tradingSignalEvaluator.checkTrend(klines, tradingSignal);

        // Assert that the result is DEFAULT
        assertThat(trend).isEqualTo(TrendingStatus.DEFAULT);
    }

    @Test
    void testCheckTrend_ShouldReturnBuyWhenPatternDetectedAndBuyEntry() {
        tradingSignalEvaluator = spy(TradingSignalEvaluator.class);
        TradingSignal tradingSignal = mock(TradingSignal.class);
        // Setup mock Kline data
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");
            kline.setLowPrice("1.0");
            kline.setClosePrice("1.1");
            klines.add(kline);
        }

        // Mock the static method HarmonicPatterns.detectABCDPattern
        try (MockedStatic<HarmonicPatterns> mockedStatic = mockStatic(HarmonicPatterns.class)) {
            mockedStatic.when(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()))
                    .thenReturn(true);

            // Mock the generated TradingSignal and its behavior
            doReturn(tradingSignal).when(tradingSignalEvaluator).generateSignal(klines, "BTCUSDT");
            when(tradingSignal.isBuyEntry(true)).thenReturn(true);
            when(tradingSignal.isSellEntry(true)).thenReturn(false);

            // Call the method to test
            TrendingStatus trendingStatus = tradingSignalEvaluator.checkTrend(klines, "BTCUSDT");

            // Assert the result is BUY
            assertThat(trendingStatus).isEqualTo(TrendingStatus.BUY);

            // Verify the static method was called
            mockedStatic.verify(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()), times(1));
        }
    }

    @Test
    void testCheckTrend_ShouldReturnSellWhenPatternDetectedAndSellEntry() {
        tradingSignalEvaluator = spy(TradingSignalEvaluator.class);
        TradingSignal tradingSignal = mock(TradingSignal.class);
        // Setup mock Kline data
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");
            kline.setLowPrice("1.0");
            kline.setClosePrice("1.1");
            klines.add(kline);
        }

        // Mock the static method HarmonicPatterns.detectABCDPattern
        try (MockedStatic<HarmonicPatterns> mockedStatic = mockStatic(HarmonicPatterns.class)) {
            mockedStatic.when(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()))
                    .thenReturn(true);

            // Mock the generated TradingSignal and its behavior
            when(tradingSignal.isBuyEntry(true)).thenReturn(false);
            when(tradingSignal.isSellEntry(true)).thenReturn(true);
            doReturn(tradingSignal).when(tradingSignalEvaluator).generateSignal(klines, "BTCUSDT");


            // Call the method to test
            TrendingStatus trendingStatus = tradingSignalEvaluator.checkTrend(klines, "BTCUSDT");

            // Assert the result is SELL
            assertThat(trendingStatus).isEqualTo(TrendingStatus.SELL);

            // Verify the static method was called
            mockedStatic.verify(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()), times(1));
        }
    }

    @Test
    void testCheckTrend_ShouldReturnDefaultWhenNoPatternDetected() {
        tradingSignalEvaluator = spy(TradingSignalEvaluator.class);
        TradingSignal tradingSignal = mock(TradingSignal.class);
        // Setup mock Kline data
        List<KlineData> klines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            KlineData kline = new KlineData();
            kline.setHighPrice("1.2");
            kline.setLowPrice("1.0");
            kline.setClosePrice("1.1");
            klines.add(kline);
        }

        // Mock the static method HarmonicPatterns.detectABCDPattern
        try (MockedStatic<HarmonicPatterns> mockedStatic = mockStatic(HarmonicPatterns.class)) {
            mockedStatic.when(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()))
                    .thenReturn(false);

            // Mock the generated TradingSignal and its behavior
            doReturn(tradingSignal).when(tradingSignalEvaluator).generateSignal(klines, "BTCUSDT");

            // Call the method to test
            TrendingStatus trendingStatus = tradingSignalEvaluator.checkTrend(klines, "BTCUSDT");

            // Assert the result is DEFAULT
            assertThat(trendingStatus).isEqualTo(TrendingStatus.DEFAULT);

            // Verify the static method was called
            mockedStatic.verify(() -> HarmonicPatterns.detectABCDPattern(any(), any(), any(), any(), any()), times(1));
        }
    }
}

