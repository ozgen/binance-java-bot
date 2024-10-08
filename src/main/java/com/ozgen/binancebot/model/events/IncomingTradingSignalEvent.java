package com.ozgen.binancebot.model.events;

import com.ozgen.binancebot.model.telegram.TradingSignal;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
public class IncomingTradingSignalEvent extends ApplicationEvent {

    private final TradingSignal tradingSignal;
    public IncomingTradingSignalEvent(Object source, TradingSignal tradingSignal) {
        super(source);
        this.tradingSignal = tradingSignal;
    }

    public TradingSignal getTradingSignal() {
        return tradingSignal;
    }

}
