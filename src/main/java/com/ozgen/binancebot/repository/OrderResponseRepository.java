package com.ozgen.binancebot.repository;

import com.ozgen.binancebot.model.binance.OrderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderResponseRepository extends JpaRepository<OrderResponse, String> {

}
