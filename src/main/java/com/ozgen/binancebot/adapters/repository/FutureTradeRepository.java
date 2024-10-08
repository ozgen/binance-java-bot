package com.ozgen.binancebot.adapters.repository;

import com.ozgen.binancebot.model.TradeStatus;
import com.ozgen.binancebot.model.bot.FutureTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutureTradeRepository extends JpaRepository<FutureTrade, String> {

    List<FutureTrade> findByTradeStatus(TradeStatus tradeStatus);
    List<FutureTrade> findByTradeSignalId(String tradingSignalId);
    List<FutureTrade> findAllByTradeSignalIdIn(List<String> tradingSignalIdList);
}
