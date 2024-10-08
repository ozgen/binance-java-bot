package com.ozgen.binancebot.scheduling.bot;

import com.ozgen.binancebot.manager.bot.FutureTradeManager;
import com.ozgen.binancebot.model.TradeStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsufficientFutureTradeScheduler {

    private static final Logger log = LoggerFactory.getLogger(InsufficientFutureTradeScheduler.class);

    private final FutureTradeManager futureTradeManager;

    @Scheduled(fixedRateString = "#{${app.bot.schedule.insufficient}}")
    public void processInsufficientFutureTrades() {
        log.info("InsufficientFutureTradeScheduler has been started");
        this.futureTradeManager.processFutureTrades(TradeStatus.INSUFFICIENT);
        log.info("InsufficientFutureTradeScheduler has been finished");
    }
}
