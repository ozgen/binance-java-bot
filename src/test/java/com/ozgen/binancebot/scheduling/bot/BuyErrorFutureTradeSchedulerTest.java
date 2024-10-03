package com.ozgen.binancebot.scheduling.bot;


import com.ozgen.binancebot.manager.bot.FutureTradeManager;
import com.ozgen.binancebot.model.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class BuyErrorFutureTradeSchedulerTest {

    @Mock
    private FutureTradeManager futureTradeManager;

    @InjectMocks
    private BuyErrorFutureTradeScheduler buyErrorFutureTradeScheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessBuyErrorFutureTrades(){
        this.buyErrorFutureTradeScheduler.processBuyErrorFutureTrades();

        verify(this.futureTradeManager)
                .processFutureTrades(TradeStatus.ERROR_BUY);
    }
}
