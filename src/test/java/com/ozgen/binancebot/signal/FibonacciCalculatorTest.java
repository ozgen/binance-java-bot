package com.ozgen.binancebot.signal;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FibonacciCalculatorTest {

    @Test
    void testFibLevel() {
        // Given
        double high = 100.0;
        double low = 50.0;
        double ratio = 0.618;

        // When
        double result = FibonacciCalculator.fibLevel(high, low, ratio);

        // Then
        assertThat(result).isEqualTo(100.0 - (100.0 - 50.0) * 0.618);
    }

    @Test
    void testCalculateFibonacciLevels() {
        // Given
        double high = 100.0;
        double low = 50.0;

        // When
        double[] fibonacciLevels = FibonacciCalculator.calculateFibonacciLevels(high, low);

        // Then
        assertThat(fibonacciLevels).hasSize(6);

        // Check each Fibonacci level
        assertThat(fibonacciLevels[0]).isEqualTo(100.0 - (100.0 - 50.0) * 0.236);
        assertThat(fibonacciLevels[1]).isEqualTo(100.0 - (100.0 - 50.0) * 0.382);
        assertThat(fibonacciLevels[2]).isEqualTo(100.0 - (100.0 - 50.0) * 0.500);
        assertThat(fibonacciLevels[3]).isEqualTo(100.0 - (100.0 - 50.0) * 0.618);
        assertThat(fibonacciLevels[4]).isEqualTo(100.0 - (100.0 - 50.0) * 0.764);
        assertThat(fibonacciLevels[5]).isEqualTo(100.0 - (100.0 - 50.0) * 1.000);
    }

    @Test
    void testFibonacciLevelsWithZeroRange() {
        // Given
        double high = 100.0;
        double low = 100.0;  // No price range

        // When
        double[] fibonacciLevels = FibonacciCalculator.calculateFibonacciLevels(high, low);

        // Then
        // All Fibonacci levels should be equal to the high price since high == low
        assertThat(fibonacciLevels).containsExactly(100.0, 100.0, 100.0, 100.0, 100.0, 100.0);
    }

    @Test
    void testFibonacciLevelsWithNegativeRange() {
        // Given
        double high = 50.0;
        double low = 100.0;  // Reverse price range

        // When
        double[] fibonacciLevels = FibonacciCalculator.calculateFibonacciLevels(high, low);

        // Then
        // The levels should reflect a descending price range
        assertThat(fibonacciLevels[0]).isEqualTo(50.0 - (50.0 - 100.0) * 0.236);
        assertThat(fibonacciLevels[1]).isEqualTo(50.0 - (50.0 - 100.0) * 0.382);
        assertThat(fibonacciLevels[2]).isEqualTo(50.0 - (50.0 - 100.0) * 0.500);
        assertThat(fibonacciLevels[3]).isEqualTo(50.0 - (50.0 - 100.0) * 0.618);
        assertThat(fibonacciLevels[4]).isEqualTo(50.0 - (50.0 - 100.0) * 0.764);
        assertThat(fibonacciLevels[5]).isEqualTo(50.0 - (50.0 - 100.0) * 1.000);
    }
}
