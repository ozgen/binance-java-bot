package com.ozgen.binancebot.adapters.events;


import com.ozgen.binancebot.manager.binance.BinanceTradingSignalManager;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class IncomingTradingSignalEventListenerTest {
    @Mock
    private BinanceTradingSignalManager binanceTradingSignalManager;

    @InjectMocks
    private IncomingTradingSignalEventListener incomingTradingSignalEventListener;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testOnApplicationEvent() {
        IncomingTradingSignalEvent event = new IncomingTradingSignalEvent(this, new TradingSignal());

        this.incomingTradingSignalEventListener.onApplicationEvent(event);

        verify(this.binanceTradingSignalManager)
                .processIncomingTradingSignalEvent(event);
    }
}
