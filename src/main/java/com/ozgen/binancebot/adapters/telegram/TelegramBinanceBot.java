package com.ozgen.binancebot.adapters.telegram;

import com.ozgen.binancebot.configuration.telegram.TelegramConfig;
import com.ozgen.binancebot.manager.telegram.TelegramMessageManager;
import com.ozgen.binancebot.model.TradingStrategy;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "bot.telegram.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class TelegramBinanceBot extends AbilityBot {

    private final TelegramMessageManager telegramMessageManager;
    private final TelegramConfig telegramConfig;

    // Store user inputs
    private Map<Long, String> userSymbols = new HashMap<>();
    private Map<Long, String> userStrategies = new HashMap<>();

    @Getter
    private Long channelId;

    @Autowired
    public TelegramBinanceBot(TelegramMessageManager telegramMessageManager, TelegramConfig telegramConfig, TelegramBotsApi telegramBotsApi) throws TelegramApiException {
        super(telegramConfig.getTelegramApiToken(), telegramConfig.getBotUserName());
        this.telegramMessageManager = telegramMessageManager;
        this.telegramConfig = telegramConfig;
        telegramBotsApi.registerBot(this);
    }

    @Override
    public String getBotToken() {
        return this.telegramConfig.getTelegramApiToken();
    }

    @Override
    public String getBotUsername() {
        return this.telegramConfig.getBotUserName();
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null) {
            Long chatId = update.getMessage().getChatId();
            String userText = update.getMessage().getText();
            this.channelId = chatId;
            // If the user hasn't provided a symbol, ask for it
            userSymbols.put(chatId, userText);  // Store the user's coin symbol
            this.askForStrategy(chatId);  // Ask the user for the strategy next
        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String selectedStrategy = update.getCallbackQuery().getData();

            // Store the selected strategy
            userStrategies.put(chatId, selectedStrategy);
            this.processTrade(chatId);  // Process the trade with the selected strategy

            this.cleanupUserSession(chatId);

        }
    }

    private void askForStrategy(Long chatId) {
        // Create an inline keyboard with strategy options
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Add buttons for each strategy
        for (TradingStrategy strategy : TradingStrategy.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(strategy.name());
            button.setCallbackData(strategy.name());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        // Send a message asking for the strategy with inline buttons
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Please choose a trading strategy:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }

    private void processTrade(Long chatId) {
        // Now we have both the symbol and the strategy, process the trade
        String symbol = userSymbols.get(chatId);
        String strategy = userStrategies.get(chatId);

        // Trigger the TelegramMessageManager to parse the symbol and strategy
        String message = this.telegramMessageManager.parseTelegramMessage(symbol, TradingStrategy.valueOf(strategy));
        SendMessage response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText("Processing trade for symbol: " + symbol + " with strategy: " + strategy + "\n" + message);

        try {
            execute(response);
        } catch (Exception e) {
            log.error("Failed to send message: {}", e.getMessage());
        }

        // Optionally clear the session to reset for future interactions
        userSymbols.remove(chatId);
        userStrategies.remove(chatId);
    }

    private void cleanupUserSession(Long chatId) {
        // Remove the user's symbol and strategy to reset the session
        userSymbols.remove(chatId);
        userStrategies.remove(chatId);
        log.info("Cleaned up session for chatId: {}", chatId);
    }

    @PostConstruct
    public void start() {
        log.info("Bot started with username: {}, token: {}", telegramConfig.getBotUserName(), telegramConfig.getTelegramApiToken());
    }

    public Map<Long, String> getUserSymbols() {
        return userSymbols;
    }

    public Map<Long, String> getUserStrategies() {
        return userStrategies;
    }

}
