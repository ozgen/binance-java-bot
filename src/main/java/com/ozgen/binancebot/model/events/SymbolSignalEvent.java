package com.ozgen.binancebot.model.events;

import com.ozgen.binancebot.model.TradingStrategy;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
public class SymbolSignalEvent extends ApplicationEvent {

    private final String symbol;
    private final TradingStrategy tradingStrategy;
    public SymbolSignalEvent(Object source, String symbol, TradingStrategy tradingStrategy) {
        super(source);
        this.symbol = symbol;
        this.tradingStrategy = tradingStrategy;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradingStrategy getTradingStrategy() {
        return tradingStrategy;
    }
}
