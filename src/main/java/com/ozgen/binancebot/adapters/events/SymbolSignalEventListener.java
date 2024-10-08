package com.ozgen.binancebot.adapters.events;

import com.ozgen.binancebot.manager.binance.TradingSignalEvaluationManager;
import com.ozgen.binancebot.model.events.SymbolSignalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SymbolSignalEventListener implements ApplicationListener<SymbolSignalEvent> {

    private final TradingSignalEvaluationManager tradingSignalEvaluationManager;

    @Override
    public void onApplicationEvent(SymbolSignalEvent event) {
        log.info("new SymbolSignalEvent consumed. event : {}", event);
        this.tradingSignalEvaluationManager.processSymbolSignalEvent(event);
    }
}
