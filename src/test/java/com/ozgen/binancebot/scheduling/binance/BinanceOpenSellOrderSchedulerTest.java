package com.ozgen.binancebot.scheduling.binance;


import com.ozgen.binancebot.manager.binance.BinanceOpenSellOrderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class BinanceOpenSellOrderSchedulerTest {
    @Mock
    private BinanceOpenSellOrderManager binanceOpenSellOrderManager;

    @InjectMocks
    private BinanceOpenSellOrderScheduler binanceOpenSellOrderScheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessOpenSellOrders() {
        this.binanceOpenSellOrderScheduler.processOpenSellOrders();

        verify(this.binanceOpenSellOrderManager)
                .processOpenSellOrders();
    }
}
