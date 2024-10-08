package com.ozgen.binancebot.manager.telegram;

import com.ozgen.binancebot.manager.binance.BinanceApiManager;
import com.ozgen.binancebot.model.TradingStrategy;
import com.ozgen.binancebot.model.events.SymbolSignalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageManager {

    static final String SUCCESS_MESSAGE = "The post with %s has been received successfully";
    static final String FAILED_MESSAGE = "The post is not parsed.";

    private final ApplicationEventPublisher publisher;
    private final BinanceApiManager binanceApiManager;

    public String parseTelegramMessage(String symbol, TradingStrategy tradingStrategy) {

        if (symbol.isEmpty() || !this.binanceApiManager.checkSymbol(symbol.toUpperCase())) {
            log.warn("invalid data comes from telegram message: '{}'", symbol);
            return FAILED_MESSAGE;
        }

        SymbolSignalEvent event = new SymbolSignalEvent(this, symbol.toUpperCase(), tradingStrategy);
        this.publisher.publishEvent(event);

        return String.format(SUCCESS_MESSAGE, symbol);
    }
}
