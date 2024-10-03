package com.ozgen.binancebot.adapters.events;

import com.ozgen.binancebot.manager.binance.BinanceSellOrderManager;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewSellOrderEventListener implements ApplicationListener<NewSellOrderEvent> {


    private final BinanceSellOrderManager binanceSellOrderManager;

    @Override
    public void onApplicationEvent(NewSellOrderEvent event) {
        log.info("new NewSellOrderEvent consumed. event : {}", event);
        this.binanceSellOrderManager.processNewSellOrderEvent(event);
    }
}
