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
public class BuyErrorFutureTradeScheduler {
    private static final Logger log = LoggerFactory.getLogger(BuyErrorFutureTradeScheduler.class);


    private final FutureTradeManager futureTradeManager;

    @Scheduled(fixedRateString = "#{${app.bot.schedule.buyError}}")
    public void processBuyErrorFutureTrades() {
        log.info("BuyErrorFutureTradeScheduler has been started");
        this.futureTradeManager.processFutureTrades(TradeStatus.ERROR_BUY);
        log.info("BuyErrorFutureTradeScheduler has been finished");
    }
}
