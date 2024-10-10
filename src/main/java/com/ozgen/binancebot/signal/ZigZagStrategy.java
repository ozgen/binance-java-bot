package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.configuration.properties.BotConfiguration;
import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZigZagStrategy {

    private final BotConfiguration botConfiguration;

    public List<Double> calculateZigZag(List<Double> prices) {
        List<Double> zigZagPoints = new ArrayList<>();
        if (prices == null || prices.size() < 2) {
            return zigZagPoints;
        }
        double zigZagPercent = this.botConfiguration.getZigzagPercentage();
        double lastHigh = prices.get(0);
        double lastLow = prices.get(0);
        zigZagPoints.add(prices.get(0)); // Add the first point

        for (int i = 1; i < prices.size(); i++) {
            double currentPrice = prices.get(i);

            // If the current price moves up more than the zigzag percentage from the last low
            if (currentPrice >= lastLow + (lastLow * zigZagPercent / 100)) {
                zigZagPoints.add(currentPrice);
                lastHigh = currentPrice;  // Update the last high
            }

            // If the current price moves down more than the zigzag percentage from the last high
            if (currentPrice <= lastHigh - (lastHigh * zigZagPercent / 100)) {
                zigZagPoints.add(currentPrice);
                lastLow = currentPrice;  // Update the last low
            }
        }
        return zigZagPoints;
    }

    public double[] getLastTwoZigZagPoints(List<KlineData> klines) {
        double zigzagHigh = 0;
        double zigzagLow = Double.MAX_VALUE;

        int highIndex = -1;
        int lowIndex = -1;
        double threshold = this.botConfiguration.getZigzagPercentage();

        // Iterate over the klines in reverse to get the most recent two ZigZag points
        for (int i = klines.size() - 2; i >= 0; i--) {
            double currentPrice = GenericParser.getFormattedDouble(klines.get(i).getClosePrice());
            double nextPrice = GenericParser.getFormattedDouble(klines.get(i + 1).getClosePrice());

            // Check if a high ZigZag point is found
            if ((nextPrice - currentPrice) / currentPrice >= threshold && highIndex == -1) {
                zigzagHigh = currentPrice;
                highIndex = i;
            }

            // Check if a low ZigZag point is found
            if ((currentPrice - nextPrice) / nextPrice >= threshold && lowIndex == -1) {
                zigzagLow = currentPrice;
                lowIndex = i;
            }

            // If both high and low points are found, break
            if (highIndex != -1 && lowIndex != -1) {
                break;
            }
        }

        // Ensure we found two valid ZigZag points
        if (highIndex != -1 && lowIndex != -1) {
            return new double[]{zigzagHigh, zigzagLow};
        }

        // Return null if the points weren't found
        return null;
    }
}
