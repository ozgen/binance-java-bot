package com.ozgen.binancebot.signal;

public class FibonacciCalculator {

    // Computes the Fibonacci retracement level based on the price range and Fibonacci ratio
    public static double fibLevel(double high, double low, double ratio) {
        return high - (high - low) * ratio;
    }

    // Standard Fibonacci retracement levels
    public static double[] calculateFibonacciLevels(double high, double low) {
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
