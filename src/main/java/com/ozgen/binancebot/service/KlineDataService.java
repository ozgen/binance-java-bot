package com.ozgen.binancebot.service;

import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.repository.KlineDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KlineDataService {

    private static final Logger log = LoggerFactory.getLogger(KlineDataService.class);

    private final KlineDataRepository klineDataRepository;

    public KlineData createKlineData(KlineData klineData) {
        try {
            KlineData saved = this.klineDataRepository.save(klineData);
            log.info("KlineData created: {}", saved);
            return saved;
        } catch (Exception e) {
            log.error("Error creating KlineData: {}", e.getMessage(), e);
            return klineData;
        }
    }
}

