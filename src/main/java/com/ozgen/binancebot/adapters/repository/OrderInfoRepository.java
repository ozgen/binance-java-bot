package com.ozgen.binancebot.adapters.repository;

import com.ozgen.binancebot.model.binance.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, String> {

}
