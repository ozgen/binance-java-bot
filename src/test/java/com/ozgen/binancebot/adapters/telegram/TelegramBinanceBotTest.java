package com.ozgen.binancebot.adapters.telegram;


import com.ozgen.binancebot.configuration.telegram.TelegramConfig;
import com.ozgen.binancebot.manager.telegram.TelegramMessageManager;
import com.ozgen.binancebot.model.TradingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramBinanceBotTest {

    @Mock
    private TelegramMessageManager telegramMessageManager;

    @Mock
    private TelegramConfig telegramConfig;

    @Mock
    private TelegramBotsApi telegramBotsApi;

    private TelegramBinanceBot telegramBinanceBot;

    @BeforeEach
    void setUp() throws TelegramApiException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOnUpdateReceivedWithSymbol() throws TelegramApiException {
        String symbol = "BNBBTC";
        Long chatId = 12345L;
        when(telegramConfig.getTelegramApiToken()).thenReturn("test-token");
        when(telegramConfig.getBotUserName()).thenReturn("test-bot");
        telegramBinanceBot = spy(new TelegramBinanceBot(telegramMessageManager, telegramConfig, telegramBotsApi) {
            @Override
            public long creatorId() {
                return 12345L; // Sample creator ID
            }
        });
        // Mocking the Update object
        Update mockUpdate = mock(Update.class);
        Message mockMessage = mock(Message.class);
        Chat mockChat = mock(Chat.class);

        when(mockUpdate.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChatId()).thenReturn(chatId);
        when(mockMessage.getText()).thenReturn(symbol);
        when(telegramConfig.getBotUserName()).thenReturn("test-bot2");

        // Call the method
        telegramBinanceBot.onUpdateReceived(mockUpdate);

        // Verify that the bot asks for the strategy after receiving the symbol
        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(chatId.toString());
        expectedMessage.setText("Please choose a trading strategy:");

        verify(telegramMessageManager, never())
                .parseTelegramMessage(anyString(), any());
        verify(telegramBinanceBot)
                .execute(any(SendMessage.class));

        // Assert that the symbol was stored correctly
        assertThat(telegramBinanceBot.getUserSymbols()).containsEntry(chatId, symbol);
    }

    @Test
    void testOnUpdateReceivedWithStrategySelection() throws TelegramApiException {
        when(telegramConfig.getTelegramApiToken()).thenReturn("test-token-2");
        when(telegramConfig.getBotUserName()).thenReturn("test-bot-2");
        telegramBinanceBot = spy(new TelegramBinanceBot(telegramMessageManager, telegramConfig, telegramBotsApi) {
            @Override
            public long creatorId() {
                return 12345L; // Sample creator ID
            }
        });
        String symbol = "BNBBTC";
        Long chatId = 12345L;
        String selectedStrategy = TradingStrategy.SELL_LATER.name();
        String parsedMessage = "Trade parsed successfully";

        // Simulate user previously provided a symbol
        telegramBinanceBot.getUserSymbols().put(chatId, symbol);

        // Mocking the Update and CallbackQuery
        Update mockUpdate = mock(Update.class);
        CallbackQuery mockCallbackQuery = mock(CallbackQuery.class);
        Message mockMessage = mock(Message.class);

        when(mockUpdate.hasCallbackQuery()).thenReturn(true);
        when(mockUpdate.getCallbackQuery()).thenReturn(mockCallbackQuery);
        when(mockCallbackQuery.getMessage()).thenReturn(mockMessage);
        when(mockMessage.getChatId()).thenReturn(chatId);
        when(mockCallbackQuery.getData()).thenReturn(selectedStrategy);

        // Mock the telegram message parsing
        when(telegramMessageManager.parseTelegramMessage(symbol, TradingStrategy.SELL_LATER)).thenReturn(parsedMessage);

        // Call the method
        telegramBinanceBot.onUpdateReceived(mockUpdate);

        // Verify that the bot processed the trade and sent the response
        SendMessage expectedResponse = new SendMessage();
        expectedResponse.setChatId(chatId.toString());
        expectedResponse.setText("Processing trade for symbol: " + symbol + " with strategy: SELL_LATER\n" + parsedMessage);
        verify(telegramBinanceBot)
                .execute(any(SendMessage.class));

        // Assert that the session is cleaned up
        assertThat(telegramBinanceBot.getUserSymbols()).doesNotContainKey(chatId);
        assertThat(telegramBinanceBot.getUserStrategies()).doesNotContainKey(chatId);
    }
}
