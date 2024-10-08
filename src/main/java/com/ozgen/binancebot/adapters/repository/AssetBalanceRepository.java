package com.ozgen.binancebot.adapters.repository;

import com.ozgen.binancebot.model.binance.AssetBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetBalanceRepository extends JpaRepository<AssetBalance, String> {

}
