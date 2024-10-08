package com.ozgen.binancebot.scheduling.binance;

import com.ozgen.binancebot.manager.binance.BinanceOpenBuyOrderManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinanceOpenBuyOrderScheduler {
    private static final Logger log = LoggerFactory.getLogger(BinanceOpenBuyOrderScheduler.class);

    private final BinanceOpenBuyOrderManager binanceOpenBuyOrderManager;

    @Scheduled(fixedRateString = "#{${app.bot.schedule.openBuyOrder}}")
    public void processOpenBuyOrders() {
        log.info("OpenBuyOrderScheduler has been started");
        this.binanceOpenBuyOrderManager.processOpenBuyOrders();
        log.info("OpenBuyOrderScheduler has been finished");
    }
}
