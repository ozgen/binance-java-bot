package com.ozgen.binancebot;

import com.ozgen.binancebot.adapters.telegram.TelegramBinanceBot;
import com.ozgen.binancebot.configuration.BinanceTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(BinanceTestConfig.class)
class BinanceJavaBotApplicationTests {

    @MockBean
    private TelegramBinanceBot telegramBinanceBot;

    @Test
    void contextLoads() {
    }

}
