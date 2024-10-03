package com.ozgen.binancebot.repository;

import com.ozgen.binancebot.model.binance.KlineData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KlineDataRepository extends JpaRepository<KlineData, String> {
}
