package com.ozgen.binancebot.manager.bot;

import com.ozgen.binancebot.manager.binance.BinanceApiManager;
import com.ozgen.binancebot.model.TradeStatus;
import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.bot.BuyOrder;
import com.ozgen.binancebot.model.bot.FutureTrade;
import com.ozgen.binancebot.model.events.NewBuyOrderEvent;
import com.ozgen.binancebot.model.events.NewSellOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.BotOrderService;
import com.ozgen.binancebot.service.FutureTradeService;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.utils.SyncUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FutureTradeManager {
    private final BinanceApiManager binanceApiManager;
    private final FutureTradeService futureTradeService;
    private final TradingSignalService tradingSignalService;
    private final BotOrderService botOrderService;
    private final ApplicationEventPublisher publisher;


    public void processFutureTrades(TradeStatus tradeStatus) {
        if (!this.isInsufficientOrNotInRangeOrErrorBuy(tradeStatus)) {
            log.error("trade status should be insufficient, not in range or buy error tradeStatus: '{}'", tradeStatus);
            return;
        }

        List<FutureTrade> futureTrades = this.futureTradeService.getAllFutureTradeByTradeStatus(tradeStatus);
        if (futureTrades.isEmpty()) {
            log.info("There are no {} future trades in the db.", tradeStatus);
            return;
        }

        this.processTrades(futureTrades);
    }

    public void processSellErrorFutureTrades() {
        List<FutureTrade> futureTrades = this.futureTradeService.getAllFutureTradeByTradeStatus(TradeStatus.ERROR_SELL);
        if (futureTrades.isEmpty()) {
            log.info("There are no {} future trades in the db.", TradeStatus.ERROR_SELL);
            return;
        }

        this.processSellTrades(futureTrades);
        this.futureTradeService.deleteFutureTrades(futureTrades);
    }

    private void processTrades(List<FutureTrade> futureTrades) {
        List<String> tradingSignalIdList = futureTrades.stream()
                .map(FutureTrade::getTradeSignalId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<FutureTrade>> groupedFutureTrades = this.futureTradeService.getAllFutureTradeByTradingSignals(tradingSignalIdList)
                .stream()
                .collect(Collectors.groupingBy(FutureTrade::getTradeSignalId));

        Map<String, TradingSignal> groupedSignals = this.tradingSignalService.getTradingSignalsByIdList(tradingSignalIdList)
                .stream()
                .collect(Collectors.toMap(TradingSignal::getId, Function.identity(), (existing, replacement) -> existing));

        groupedFutureTrades.forEach((tradeSignalId, tradeList) -> this.processTradeGroup(tradeSignalId, tradeList, groupedSignals));
    }

    private void processSellTrades(List<FutureTrade> futureTrades) {
        List<String> tradingSignalIdList = futureTrades.stream()
                .map(FutureTrade::getTradeSignalId)
                .distinct()
                .collect(Collectors.toList());


        List<TradingSignal> tradingSignals = this.tradingSignalService.getTradingSignalsByIdList(tradingSignalIdList);
        List<BuyOrder> buyOrders = this.botOrderService.getBuyOrders(tradingSignals);
        if (buyOrders.isEmpty()) {
            log.info("There is no buyOrder occured for these sell order error in the db.");
            return;
        }

        buyOrders.forEach(this::publishNewSellOrderEvent);
    }

    private void processTradeGroup(String tradeSignalId, List<FutureTrade> tradeList, Map<String, TradingSignal> groupedSignals) {
        if (tradeList.size() != 1) {
            this.futureTradeService.deleteFutureTrades(tradeList);
            return;
        }

        TradingSignal tradingSignal = groupedSignals.get(tradeSignalId);
        TickerData tickerPrice24 = this.fetchTickerPrice(tradingSignal);
        if (tickerPrice24 != null) {
            this.publishNewBuyOrderEvent(tradingSignal, tickerPrice24);
            this.futureTradeService.deleteFutureTrades(tradeList);
            SyncUtil.pauseBetweenOperations();
        }

    }

    private TickerData fetchTickerPrice(TradingSignal tradingSignal) {
        try {
            return this.binanceApiManager.getTickerPrice24(tradingSignal.getSymbol());
        } catch (Exception e) {
            log.error("Error occurred while executing getTickerPrice24 for symbol: {}", tradingSignal.getSymbol(), e);
            return null;
        }
    }

    private void publishNewBuyOrderEvent(TradingSignal tradingSignal, TickerData tickerPrice24) {
        NewBuyOrderEvent newBuyOrderEvent = new NewBuyOrderEvent(this, tradingSignal, tickerPrice24);
        this.publisher.publishEvent(newBuyOrderEvent);
        SyncUtil.pauseBetweenOperations();
    }

    private void publishNewSellOrderEvent(BuyOrder buyOrder) {
        NewSellOrderEvent newSellOrderEvent = new NewSellOrderEvent(this, buyOrder);
        this.publisher.publishEvent(newSellOrderEvent);
    }

    private boolean isInsufficientOrNotInRangeOrErrorBuy(TradeStatus tradeStatus) {
        return tradeStatus == TradeStatus.INSUFFICIENT || tradeStatus == TradeStatus.NOT_IN_RANGE || tradeStatus == TradeStatus.ERROR_BUY;
    }
}
