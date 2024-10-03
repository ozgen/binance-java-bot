package com.ozgen.binancebot.repository;

import com.ozgen.binancebot.model.binance.OpenOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpenOrderRepository extends JpaRepository<OpenOrder, String> {

}
