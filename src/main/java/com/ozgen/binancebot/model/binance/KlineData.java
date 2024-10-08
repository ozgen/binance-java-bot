package com.ozgen.binancebot.model.binance;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Data
@Entity
@Table(name = "kline_data")
@ToString
public class KlineData {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String symbol;

    private long openTime;                // Kline open time
    private String openPrice;             // Open price
    private String highPrice;             // High price
    private String lowPrice;              // Low price
    private String closePrice;            // Close price
    private String volume;                // Volume
    private long closeTime;               // Kline close time
    private String quoteAssetVolume;      // Quote asset volume
    private int numberOfTrades;           // Number of trades
    private String takerBuyBaseAssetVolume;  // Taker buy base asset volume
    private String takerBuyQuoteAssetVolume; // Taker buy quote asset volume
    private Date createdAt;
    private Date updatedAt;

    public KlineData(long openTime, String openPrice, String highPrice, String lowPrice, String closePrice,
                     String volume, long closeTime, String quoteAssetVolume, int numberOfTrades,
                     String takerBuyBaseAssetVolume, String takerBuyQuoteAssetVolume) {
        this.openTime = openTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.closeTime = closeTime;
        this.quoteAssetVolume = quoteAssetVolume;
        this.numberOfTrades = numberOfTrades;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public KlineData() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
