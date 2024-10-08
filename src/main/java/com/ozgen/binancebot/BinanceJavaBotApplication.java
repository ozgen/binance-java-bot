package com.ozgen.binancebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BinanceJavaBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinanceJavaBotApplication.class, args);
    }

}
