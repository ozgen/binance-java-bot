package com.ozgen.binancebot.adapters.events;


import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.events.InfoEvent;
import com.ozgen.binancebot.service.telegram.TelegramSignalNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

class InfoEventListenerTest {

    @Mock
    private TelegramSignalNotifier telegramSignalNotifier;

    @InjectMocks
    private InfoEventListener infoEventListener;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testOnApplicationEvent() {
        InfoEvent infoEvent = new InfoEvent(this, new BuyOrder().toString());

        this.infoEventListener.onApplicationEvent(infoEvent);

        verify(this.telegramSignalNotifier)
                .processInfoEvent(infoEvent);
    }
}
