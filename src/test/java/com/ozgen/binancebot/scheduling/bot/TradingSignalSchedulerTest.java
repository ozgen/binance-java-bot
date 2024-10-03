package com.ozgen.binancebot.scheduling.bot;


import com.ozgen.binancebot.manager.bot.TradeSignalManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class TradingSignalSchedulerTest {

    @Mock
    private TradeSignalManager tradeSignalManager;

    @InjectMocks
    private TradingSignalScheduler tradingSignalScheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessInitTradingSignals(){
        this.tradingSignalScheduler.processInitTradingSignals();

        verify(this.tradeSignalManager)
                .processInitTradingSignals();
    }
}
