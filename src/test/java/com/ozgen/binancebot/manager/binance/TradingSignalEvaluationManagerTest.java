package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.events.SymbolSignalEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.signal.TradingSignalEvaluator;
import com.ozgen.binancebot.utils.validators.TradingSignalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradingSignalEvaluationManagerTest {

    @Mock
    private BinanceApiManager binanceApiManager;

    @Mock
    private TradingSignalEvaluator tradingSignalEvaluator;

    @Mock
    private TradingSignalService tradingSignalService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TradingSignalEvaluationManager tradingSignalEvaluationManager;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processSymbolSignalEvent_WithValidSignal_ShouldPublishEventAndSaveSignal() throws Exception {
        // Setup
        String symbol = "BTCUSDT";
        List<KlineData> klines = mockKlineData();
        TradingSignal tradingSignal = mock(TradingSignal.class);

        // Mocking
        when(binanceApiManager.getListOfKlineData(symbol)).thenReturn(klines);
        when(tradingSignalEvaluator.generateSignal(klines, symbol)).thenReturn(tradingSignal);

        try (MockedStatic<TradingSignalValidator> mockedValidator = mockStatic(TradingSignalValidator.class)) {
            mockedValidator.when(() -> TradingSignalValidator.validate(tradingSignal)).thenReturn(true);

            when(tradingSignalEvaluator.checkTrend(klines, tradingSignal)).thenReturn(TrendingStatus.BUY);
            when(tradingSignalService.saveTradingSignal(tradingSignal)).thenReturn(tradingSignal);

            // Creating the event
            SymbolSignalEvent event = new SymbolSignalEvent(this, symbol, null);

            // Action
            tradingSignalEvaluationManager.processSymbolSignalEvent(event);

            // Verification
            verify(tradingSignalService).saveTradingSignal(tradingSignal);
            ArgumentCaptor<IncomingTradingSignalEvent> eventCaptor = ArgumentCaptor.forClass(IncomingTradingSignalEvent.class);
            verify(publisher).publishEvent(eventCaptor.capture());

            IncomingTradingSignalEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getTradingSignal()).isEqualTo(tradingSignal);
        }
    }

    @Test
    void processSymbolSignalEvent_WithInvalidSignal_ShouldNotPublishEvent() throws Exception {
        // Setup
        String symbol = "BTCUSDT";
        List<KlineData> klines = mockKlineData();
        TradingSignal tradingSignal = mock(TradingSignal.class);

        // Mocking
        when(binanceApiManager.getListOfKlineData(symbol)).thenReturn(klines);
        when(tradingSignalEvaluator.generateSignal(klines, symbol)).thenReturn(tradingSignal);

        // Mock static method TradingSignalValidator.validate()
        try (MockedStatic<TradingSignalValidator> mockedValidator = mockStatic(TradingSignalValidator.class)) {
            mockedValidator.when(() -> TradingSignalValidator.validate(tradingSignal)).thenReturn(false);

            // Creating the event
            SymbolSignalEvent event = new SymbolSignalEvent(this, symbol, null);

            // Action
            tradingSignalEvaluationManager.processSymbolSignalEvent(event);

            // Verification
            verify(tradingSignalService, never()).saveTradingSignal(tradingSignal);
            verify(publisher, never()).publishEvent(any(IncomingTradingSignalEvent.class));
        }
    }

    @Test
    void processSymbolSignalEvent_WithInsufficientData_ShouldNotProcess() throws Exception {
        // Setup
        String symbol = "BTCUSDT";
        List<KlineData> insufficientKlines = Arrays.asList(mock(KlineData.class));

        // Mocking
        when(binanceApiManager.getListOfKlineData(symbol)).thenReturn(insufficientKlines);

        // Creating the event
        SymbolSignalEvent event = new SymbolSignalEvent(this, symbol, null);

        // Action
        tradingSignalEvaluationManager.processSymbolSignalEvent(event);

        // Verification
        verify(tradingSignalEvaluator, never()).generateSignal(any(), anyString());
        verify(tradingSignalService, never()).saveTradingSignal(any());
        verify(publisher, never()).publishEvent(any(IncomingTradingSignalEvent.class));
    }

    @Test
    void processSymbolSignalEvent_WhenNotBuySignal_ShouldNotSaveOrPublish() throws Exception {
        // Setup
        String symbol = "BTCUSDT";
        List<KlineData> klines = mockKlineData();
        TradingSignal tradingSignal = mock(TradingSignal.class);

        // Mocking
        when(binanceApiManager.getListOfKlineData(symbol)).thenReturn(klines);
        when(tradingSignalEvaluator.generateSignal(klines, symbol)).thenReturn(tradingSignal);

        // Mock static method TradingSignalValidator.validate()
        try (MockedStatic<TradingSignalValidator> mockedValidator = mockStatic(TradingSignalValidator.class)) {
            mockedValidator.when(() -> TradingSignalValidator.validate(tradingSignal)).thenReturn(true);
            when(tradingSignalEvaluator.checkTrend(klines, tradingSignal)).thenReturn(TrendingStatus.DEFAULT);

            // Creating the event
            SymbolSignalEvent event = new SymbolSignalEvent(this, symbol, null);

            // Action
            tradingSignalEvaluationManager.processSymbolSignalEvent(event);

            // Verification
            verify(tradingSignalService, never()).saveTradingSignal(tradingSignal);
            verify(publisher, never()).publishEvent(any(IncomingTradingSignalEvent.class));
        }
    }

    // Utility method to mock Kline data
    private List<KlineData> mockKlineData() {
        KlineData kline1 = mock(KlineData.class);
        KlineData kline2 = mock(KlineData.class);
        KlineData kline3 = mock(KlineData.class);
        KlineData kline4 = mock(KlineData.class);
        KlineData kline5 = mock(KlineData.class);

        when(kline1.getHighPrice()).thenReturn("1.2");
        when(kline1.getLowPrice()).thenReturn("1.0");
        when(kline2.getHighPrice()).thenReturn("1.2");
        when(kline2.getLowPrice()).thenReturn("1.0");
        when(kline3.getHighPrice()).thenReturn("1.2");
        when(kline3.getLowPrice()).thenReturn("1.0");
        when(kline4.getHighPrice()).thenReturn("1.2");
        when(kline4.getLowPrice()).thenReturn("1.0");
        when(kline5.getHighPrice()).thenReturn("1.2");
        when(kline5.getLowPrice()).thenReturn("1.0");

        return Arrays.asList(kline1, kline2, kline3, kline4, kline5);
    }
}

