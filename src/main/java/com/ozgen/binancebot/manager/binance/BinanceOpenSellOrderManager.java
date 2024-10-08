package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.configuration.properties.ScheduleConfiguration;
import com.ozgen.binancebot.model.ProcessStatus;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.bot.SellOrder;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.BotOrderService;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.DateFactory;
import com.ozgen.binancebot.utils.SyncUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceOpenSellOrderManager {

    private final TradingSignalService tradingSignalService;
    private final BotOrderService botOrderService;
    private final ApplicationEventPublisher publisher;
    private final ScheduleConfiguration scheduleConfiguration;

    public void processOpenSellOrders() {
        Date searchDate = getSearchDate();
        List<TradingSignal> tradingSignals = this.tradingSignalService
                .getDefaultTradingSignalsAfterDateAndIsProcessIn(searchDate, List.of(ProcessStatus.BUY));
        if (tradingSignals.isEmpty()) {
            log.info("No trading signal has been detected.");
            return;
        }

        List<BuyOrder> buyOrders = this.botOrderService.getBuyOrders(tradingSignals);

        buyOrders.forEach(this::safelyPublishNewSellOrder);
    }

    public void processNotCompletedSellOrders() {
        Date searchDate = getSearchDate();
        List<TradingSignal> sellSignals = this.tradingSignalService
                .getDefaultTradingSignalsAfterDateAndIsProcessIn(searchDate, List.of(ProcessStatus.SELL));

        if (sellSignals.isEmpty()) {
            log.info("No trading signal has been detected.");
            return;
        }
        List<SellOrder> sellOrders = this.botOrderService.getSellOrders(sellSignals);
        List<BuyOrder> buyOrders = this.botOrderService.getBuyOrders(sellSignals);
        List<BuyOrder> matchingOrders = this.findMatchingOrders(sellOrders, buyOrders);
        if (matchingOrders.isEmpty()) {
            log.info("No matching has been detected.");
            return;
        }
        matchingOrders.forEach(this::safelyPublishNewSellOrder);
    }

    private List<BuyOrder> findMatchingOrders(List<SellOrder> sellOrders, List<BuyOrder> buyOrders) {
        return buyOrders.stream()
                .filter(buyOrder -> {
                    TradingSignal buySignal = buyOrder.getTradingSignal();
                    return sellOrders.stream()
                            .anyMatch(sellOrder -> sellOrder.getTradingSignal().getId().equals(buySignal.getId()) &&
                                    sellOrder.getCoinAmount() < buyOrder.getCoinAmount());
                })
                .collect(Collectors.toList());
    }

    private void safelyPublishNewSellOrder(BuyOrder buyOrder) {
        try {
            this.publishNewSellOrder(buyOrder);
            SyncUtil.pauseBetweenOperations();
        } catch (Exception e) {
            log.error("Error while processing sell order for BuyOrder ID {}: {}", buyOrder.getId(), e.getMessage(), e);
        }
    }

    private void publishNewSellOrder(BuyOrder buyOrder) {
        NewSellOrderEvent newSellOrderEvent = new NewSellOrderEvent(this, buyOrder);
        this.publisher.publishEvent(newSellOrderEvent);
        log.info("Published NewSellOrderEvent for BuyOrder {}", buyOrder.getId());
    }

    private Date getSearchDate() {
        return DateFactory.getDateBeforeInMonths(this.scheduleConfiguration.getMonthBefore());
    }
}
