package com.ozgen.binancebot.adapters.events;

import com.ozgen.binancebot.manager.binance.BinanceTradingSignalManager;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncomingTradingSignalEventListener implements ApplicationListener<IncomingTradingSignalEvent> {
    private final BinanceTradingSignalManager binanceTradingSignalManager;

    @Override
    public void onApplicationEvent(IncomingTradingSignalEvent event) {
        log.info("new IncomingTradingSignalEvent consumed. event : {}", event);
        this.binanceTradingSignalManager.processIncomingTradingSignalEvent(event);
    }
}
