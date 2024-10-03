package com.ozgen.binancebot.adapters.events;

import com.ozgen.binancebot.manager.binance.BinanceBuyOrderManager;
import com.ozgen.binancebot.model.events.NewBuyOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewBuyOrderEventListener implements ApplicationListener<NewBuyOrderEvent> {


    private final BinanceBuyOrderManager binanceBuyOrderManager;

    @Override
    public void onApplicationEvent(NewBuyOrderEvent event) {
        log.info("new NewBuyOrderEvent consumed. event : {}", event);
        this.binanceBuyOrderManager.processNewBuyOrderEvent(event);
    }
}
