package com.ozgen.binancebot.manager.telegram;


import com.ozgen.binancebot.service.TradingSignalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

public class TelegramMessageManagerTest {

    @Mock
    private TradingSignalService tradingSignalService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TelegramMessageManager telegramMessageManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testParseTelegramMessage_withValidSignal() {
        // todo write unit test
    }

    @Test
    void testParseTelegramMessage_withInvalidSignal() {
        // todo write unit test
    }
}
