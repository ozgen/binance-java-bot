package com.ozgen.binancebot.signal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FibonacciCalculator {

    // Computes the Fibonacci retracement level based on the price range and Fibonacci ratio
    public double fibLevel(double high, double low, double ratio) {
        return high - (high - low) * ratio;
    }

    // Standard Fibonacci retracement levels
    public double[] calculateFibonacciLevels(double high, double low) {
        return new double[] {
                fibLevel(high, low, 0.236),
                fibLevel(high, low, 0.382),
                fibLevel(high, low, 0.500),
                fibLevel(high, low, 0.618),
                fibLevel(high, low, 0.764),
                fibLevel(high, low, 1.000)
        };
    }
}
