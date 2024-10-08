package com.ozgen.binancebot.adapters.repository;

import com.ozgen.binancebot.model.binance.TickerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickerDataRepository extends JpaRepository<TickerData, String> {

}
