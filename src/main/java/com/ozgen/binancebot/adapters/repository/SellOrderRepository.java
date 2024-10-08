package com.ozgen.binancebot.adapters.repository;

import com.ozgen.binancebot.model.bot.SellOrder;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellOrderRepository extends JpaRepository<SellOrder, String> {

    Optional<SellOrder> findByTradingSignal(TradingSignal tradingSignal);
    List<SellOrder> findByTradingSignalIn(List<TradingSignal> tradingSignals);


}
