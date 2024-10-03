package com.ozgen.binancebot.repository;

import com.ozgen.binancebot.model.binance.CancelAndNewOrderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelAndNewOrderResponseRepository extends JpaRepository<CancelAndNewOrderResponse, String> {

}
