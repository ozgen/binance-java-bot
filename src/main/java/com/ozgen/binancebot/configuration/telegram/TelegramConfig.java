package com.ozgen.binancebot.configuration.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Value("${bot.telegram.bot_username}")
    private String botUserName;

    @Value("${bot.telegram.token}")
    private String telegramApiToken;

    public String getBotUserName() {
        return botUserName;
    }

    public String getTelegramApiToken() {
        return telegramApiToken;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
