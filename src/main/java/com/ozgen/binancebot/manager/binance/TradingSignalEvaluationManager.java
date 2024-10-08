package com.ozgen.binancebot.manager.binance;

import com.ozgen.binancebot.model.ProcessStatus;
import com.ozgen.binancebot.model.TrendingStatus;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.model.events.IncomingTradingSignalEvent;
import com.ozgen.binancebot.model.events.InfoEvent;
import com.ozgen.binancebot.model.events.SymbolSignalEvent;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import com.ozgen.binancebot.service.TradingSignalService;
import com.ozgen.binancebot.signal.TradingSignalEvaluator;
import com.ozgen.binancebot.utils.validators.TradingSignalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradingSignalEvaluationManager {

    private final BinanceApiManager binanceApiManager;
    private final TradingSignalEvaluator tradingSignalEvaluator;
    private final TradingSignalService tradingSignalService;
    private final ApplicationEventPublisher publisher;

    public void processSymbolSignalEvent(SymbolSignalEvent symbolSignalEvent) {
        String symbol = symbolSignalEvent.getSymbol();
        List<KlineData> klines;
        try {
            klines = this.binanceApiManager.getListOfKlineData(symbol);
            if (klines.size() < 5) {
                log.warn("Not enough data for analysis.");
                return;
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving Kline data list");
            throw new RuntimeException(e);
        }

        TradingSignal tradingSignal = this.tradingSignalEvaluator.generateSignal(klines, symbol);
        tradingSignal.setStrategy(symbolSignalEvent.getTradingStrategy());
        boolean valid = TradingSignalValidator.validate(tradingSignal);
        if (!valid) {
            log.warn("invalid trading signal: '{}'", tradingSignal);
            return;
        }

        if (this.tradingSignalEvaluator.checkTrend(klines, tradingSignal).equals(TrendingStatus.BUY)) {
            log.info("Trade entry confirmed for symbol: {}", tradingSignal.getSymbol());
            this.sendInfoMessage(tradingSignal.toString());
            tradingSignal.setIsProcessed(ProcessStatus.INIT);
            TradingSignal saved = this.tradingSignalService.saveTradingSignal(tradingSignal);
            IncomingTradingSignalEvent event = new IncomingTradingSignalEvent(this, saved);
            this.publisher.publishEvent(event);
            this.sendInfoMessage(tradingSignal.formatTradingSignal());
        } else {
            log.info("The coin price is not available to buy symbol: {}", tradingSignal.getSymbol());
            String message = String.format("The trading signal generated with %s coin " +
                    "but the coin price is not suitable to buy, trading signal: \n%s", tradingSignal.getSymbol(),
                    tradingSignal.formatTradingSignal());
            this.sendInfoMessage(message);
        }
    }

    private void sendInfoMessage(String message) {
        InfoEvent infoEvent = new InfoEvent(this, message);
        this.publisher.publishEvent(infoEvent);
    }
}
