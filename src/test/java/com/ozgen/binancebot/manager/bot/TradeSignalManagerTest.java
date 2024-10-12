package com.ozgen.binancebot.manager.bot;


import com.ozgen.binancebot.configuration.properties.ScheduleConfiguration;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.DateFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TradeSignalManagerTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private TradingSignalService tradingSignalService;

    @Mock
    private ScheduleConfiguration scheduleConfiguration;

    @Mock
    private DateFactory dateFactory;

    @InjectMocks
    private TradeSignalManager tradeSignalManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessInitTradingSignals() {
        // Arrange
        when(scheduleConfiguration.getMonthBefore())
                .thenReturn(1);
        when(tradingSignalService.getAllTradingSignalsAfterDateAndIsProcessIn(any(), anyList()))
                .thenReturn(List.of(new TradingSignal()));

        // Act
        tradeSignalManager.processInitTradingSignals();

        // Assert
        verify(tradingSignalService)
                .getAllTradingSignalsAfterDateAndIsProcessIn(any(), anyList());
        verify(publisher, times(1))
                .publishEvent(any(IncomingTradingSignalEvent.class));
    }

    @Test
    public void testProcessTradingSignal() {
        // Arrange
        TradingSignal tradingSignal = new TradingSignal();
        tradingSignal.setId(UUID.randomUUID().toString());

        // Act
        tradeSignalManager.processTradingSignal(tradingSignal);

        // Assert
        verify(publisher)
                .publishEvent(any(IncomingTradingSignalEvent.class));
    }
}
