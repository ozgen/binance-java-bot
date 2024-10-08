package com.ozgen.binancebot.service;

import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.adapters.repository.KlineDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<KlineData> createListOfKlineData(List<KlineData> klineDataList) {
        try {
            List<KlineData> saved = this.klineDataRepository.saveAll(klineDataList);
            log.info("KlineData list created: {}", saved.stream().findFirst().get().getSymbol());
            return saved;
        } catch (Exception e) {
            log.error("Error creating KlineData list: {}", e.getMessage(), e);
            return klineDataList;
        }
    }
}

