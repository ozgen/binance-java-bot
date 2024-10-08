package com.ozgen.binancebot.repository;

import com.ozgen.binancebot.adapters.repository.TradingSignalRepository;
import com.ozgen.binancebot.model.TradingStrategy;
import com.ozgen.binancebot.model.telegram.TradingSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TradingSignalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TradingSignalRepository tradingSignalRepository;

    private TradingSignal signal1, signal2;

    @BeforeEach
    public void setUp() {
        signal1 = new TradingSignal();
        signal1.setSymbol("BNBBTC");
        signal1.setStopLoss("50000");
        signal1.setEntryStart("5000");
        signal1.setEntryEnd("50000");
        signal1.setCreatedAt(new Date());
        signal1.setIsProcessed(0);
        signal1.setStrategy(TradingStrategy.DEFAULT); // Set strategy to non-null
        entityManager.persist(signal1);

        signal2 = new TradingSignal();
        signal2.setSymbol("ETHBTC");
        signal2.setStopLoss("4000");
        signal2.setEntryStart("5000");
        signal2.setEntryEnd("50000");
        signal2.setStrategy(TradingStrategy.DEFAULT);
        signal2.setIsProcessed(1);
        signal2.setCreatedAt(new Date(System.currentTimeMillis() - 100000000)); // Date in the past
        entityManager.persist(signal2);
    }

    @Test
    public void testFindAllByIdIn() {
        List<TradingSignal> signals = tradingSignalRepository.findAllByIdInAndStrategyIn(Arrays.asList(signal1.getId(), signal2.getId()), List.of(TradingStrategy.DEFAULT));
        assertThat(signals).containsExactlyInAnyOrder(signal1, signal2);
    }

    @Test
    public void testFindAllByCreatedAtAfterAndIsProcessedIn() {
        List<TradingSignal> signals = tradingSignalRepository.findAllByCreatedAtAfterAndIsProcessedInAndStrategyIn(new Date(System.currentTimeMillis() - 50000000), Arrays.asList(0, 1), List.of(TradingStrategy.DEFAULT)); // Assuming 0 and 1 are valid process statuses
        assertThat(signals).contains(signal1);
    }

    //todo add extra unit test scenarios for sell later strategy...
}

