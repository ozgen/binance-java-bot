package com.ozgen.binancebot.adapters.events;


import com.ozgen.binancebot.manager.binance.BinanceBuyOrderManager;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.events.NewBuyOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class NewBuyOrderEventListenerTest {
    @Mock
    private BinanceBuyOrderManager binanceBuyOrderManager;

    @InjectMocks
    private NewBuyOrderEventListener newBuyOrderEventListener;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testOnApplicationEvent() {
        NewBuyOrderEvent event = new NewBuyOrderEvent(this, new TradingSignal(), new TickerData());

        this.newBuyOrderEventListener.onApplicationEvent(event);

        verify(this.binanceBuyOrderManager)
                .processNewBuyOrderEvent(event);
    }
}
