package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.model.binance.TickerData;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.events.NewBuyOrderEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.utils.validators.TradingSignalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceTradingSignalManager {

    private final BinanceApiManager binanceApiManager;
    private final ApplicationEventPublisher publisher;


    public void processIncomingTradingSignalEvent(IncomingTradingSignalEvent event) {
        TradingSignal tradingSignal = event.getTradingSignal();
        String symbol = tradingSignal.getSymbol();

        log.info("Processing incoming trading signal event for symbol: {}", symbol);

        TickerData tickerPrice24;
        try {
            tickerPrice24 = this.binanceApiManager.getTickerPrice24(symbol);
            log.info("Fetched ticker price for symbol {}: {}", symbol, tickerPrice24);
        } catch (Exception e) {
            log.error("Error occurred while fetching ticker price for symbol {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Error fetching ticker price", e);
        }

        boolean availableToBuy = TradingSignalValidator.isAvailableToBuy(tickerPrice24, tradingSignal);
        log.info("Availability to buy for symbol {}: {}", symbol, availableToBuy);

        if (availableToBuy) {
            NewBuyOrderEvent newBuyOrderEvent = new NewBuyOrderEvent(this, tradingSignal, tickerPrice24);
            log.info("Publishing NewBuyOrderEvent for symbol {}", symbol);
            this.publisher.publishEvent(newBuyOrderEvent);
        } else {
            log.info("Symbol {} is not available to buy", symbol);
        }
    }
}
