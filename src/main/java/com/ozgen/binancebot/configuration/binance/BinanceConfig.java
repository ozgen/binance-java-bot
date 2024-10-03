package com.ozgen.binancebot.configuration.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.ozgen.binancebot.configuration.binance.signature.CustomEd25519SignatureGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("!test")
public class BinanceConfig {

    @Value("${bot.binance.api.key}")
    private String apiKey;

    @Value("${bot.binance.api.secret}")
    private String secretKey;

    @Bean
    public SpotClientImpl binanceClient() throws IOException {

        CustomEd25519SignatureGenerator signatureGenerator = new CustomEd25519SignatureGenerator(secretKey);
        return new SpotClientImpl(apiKey, signatureGenerator, "https://api.binance.com");
    }
}
