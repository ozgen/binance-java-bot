package com.ozgen.binancebot.model.telegram;

import com.ozgen.binancebot.model.TradingStrategy;
import com.ozgen.binancebot.model.TrendingStatus;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Entity
@Data
@ToString
public class TradingSignal {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String symbol;
    private String entryStart;
    private String entryEnd;
    @ElementCollection
    private List<String> takeProfits;
    private String stopLoss;
    private double closePrice;     // The current close price
    private double highPrice;      // The current high price
    private double lowPrice;       // The current low price

    private Date createdAt;

    private Date updatedAt;

    private int isProcessed;
    @Enumerated(EnumType.STRING)
    private TradingStrategy strategy = TradingStrategy.DEFAULT;
    private String investAmount;
    @Enumerated(EnumType.STRING)
    private TrendingStatus trendingStatus;

    public TradingSignal() {
    }

    public TradingSignal(String symbol, String entryStart, String entryEnd, List<String> takeProfits, String stopLoss, double closePrice, double highPrice, double lowPrice) {
        this.symbol = symbol;
        this.entryStart = entryStart;
        this.entryEnd = entryEnd;
        this.takeProfits = takeProfits;
        this.stopLoss = stopLoss;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
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

    // Buy Entry Condition based on entry range and harmonic pattern result
    public boolean isBuyEntry(boolean isPatternDetected) {
        double entryStartValue = Double.parseDouble(entryStart);
        double entryEndValue = Double.parseDouble(entryEnd);
        return isPatternDetected && closePrice >= entryStartValue && closePrice <= entryEndValue;
    }

    // Sell Entry Condition based on entry range and harmonic pattern result
    public boolean isSellEntry(boolean isPatternDetected) {
        double entryStartValue = Double.parseDouble(entryStart);
        double entryEndValue = Double.parseDouble(entryEnd);
        return isPatternDetected && closePrice <= entryStartValue && closePrice >= entryEndValue;
    }

    public String formatTradingSignal() {
        StringBuilder formattedSignal = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.######");

        // Add trending status (BUY or SELL)
        formattedSignal.append("TRENDING STATUS: ").append(trendingStatus.name()).append("\n\n");

        // Format entryStart, entryEnd, and stopLoss
        String formattedEntryStart = decimalFormat.format(Double.parseDouble(entryStart));
        String formattedEntryEnd = decimalFormat.format(Double.parseDouble(entryEnd));
        String formattedStopLoss = decimalFormat.format(Double.parseDouble(stopLoss));

        // Append symbol and entry information
        formattedSignal.append(symbol).append("\n")
                .append("ENTRY_START: ").append(formattedEntryStart).append("\n")
                .append("ENTRY_END: ").append(formattedEntryEnd).append("\n");

        // Append take profits
        for (int i = 0; i < takeProfits.size(); i++) {
            formattedSignal.append("TP").append(i + 1).append(": ").append(decimalFormat.format(Double.parseDouble(takeProfits.get(i)))).append("\n");
        }

        // Append stop loss
        formattedSignal.append("\nSTOP: ").append(formattedStopLoss);

        return formattedSignal.toString();
    }
}
