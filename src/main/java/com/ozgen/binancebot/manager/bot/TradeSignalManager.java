package com.ozgen.binancebot.manager.bot;

import com.ozgen.binancebot.configuration.properties.ScheduleConfiguration;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.DateFactory;
import com.ozgen.binancebot.utils.SyncUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.ozgen.binancebot.model.ProcessStatus.INIT;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeSignalManager {

    private final ApplicationEventPublisher publisher;
    private final TradingSignalService tradingSignalService;
    private final ScheduleConfiguration scheduleConfiguration;
    private final DateFactory dateFactory;

    public void processInitTradingSignals() {
        log.info("Processing initial trading signals...");
        List<Integer> list = List.of(INIT);
        Date dateBeforeInMonths = this.dateFactory.getDateBeforeInMonths(this.scheduleConfiguration.getMonthBefore());
        log.debug("Retrieving trading signals after date: {}", dateBeforeInMonths);
        List<TradingSignal> tradingSignals = this.tradingSignalService.getAllTradingSignalsAfterDateAndIsProcessIn(dateBeforeInMonths, list);
        log.info("Found {} trading signals to process.", tradingSignals.size());
        tradingSignals.forEach(this::processTradingSignal);
    }

    void processTradingSignal(TradingSignal tradingSignal) {
        log.info("Processing trading signal: {}", tradingSignal.getId());
        IncomingTradingSignalEvent incomingTradingSignalEvent = new IncomingTradingSignalEvent(this, tradingSignal);
        this.publisher.publishEvent(incomingTradingSignalEvent);
        log.info("Published IncomingTradingSignalEvent for Trading Signal ID: {}", tradingSignal.getId());
        SyncUtil.pauseBetweenOperations();
    }
}
