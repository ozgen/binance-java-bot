package com.ozgen.binancebot.adapters.events;


import com.ozgen.binancebot.manager.binance.BinanceSellOrderManager;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class NewSellOrderEventListenerTest {
    @Mock
    private BinanceSellOrderManager binanceSellOrderManager;

    @InjectMocks
    private NewSellOrderEventListener newSellOrderEventListener;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testOnApplicationEvent() {
        NewSellOrderEvent event = new NewSellOrderEvent(this, new BuyOrder());

        this.newSellOrderEventListener.onApplicationEvent(event);

        verify(this.binanceSellOrderManager)
                .processNewSellOrderEvent(event);
    }
}
