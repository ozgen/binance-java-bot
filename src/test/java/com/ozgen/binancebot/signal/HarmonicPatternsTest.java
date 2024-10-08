package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.model.binance.KlineData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HarmonicPatternsTest {

    private KlineData x;
    private KlineData a;
    private KlineData b;
    private KlineData c;
    private KlineData d;

    @BeforeEach
    void setUp() {
        // Initialize test data
        x = new KlineData();
        a = new KlineData();
        b = new KlineData();
        c = new KlineData();
        d = new KlineData();
    }

    @Test
    void testIsABCD_ValidABCDPattern() {
        // Valid ABCD values
        double abc = 0.5;
        double bcd = 1.5;

        // Test for valid ABCD pattern
        boolean result = HarmonicPatterns.isABCD(abc, bcd);

        // Assert that the result is true
        assertThat(result).isTrue();
    }

    @Test
    void testIsABCD_InvalidABCDPattern() {
        // Invalid ABCD values
        double abc = 0.2;  // Less than 0.382
        double bcd = 0.5;  // Less than 1.13

        // Test for invalid ABCD pattern
        boolean result = HarmonicPatterns.isABCD(abc, bcd);

        // Assert that the result is false
        assertThat(result).isFalse();
    }

    @Test
    void testIsBat_ValidBatPattern() {
        // Valid Bat values
        double xab = 0.4;
        double abc = 0.5;
        double bcd = 1.7;
        double xad = 0.6;

        // Test for valid Bat pattern
        boolean result = HarmonicPatterns.isBat(xab, abc, bcd, xad);

        // Assert that the result is true
        assertThat(result).isTrue();
    }

    @Test
    void testIsBat_InvalidBatPattern() {
        // Invalid Bat values
        double xab = 0.2;  // Less than 0.382
        double abc = 0.9;  // More than 0.886
        double bcd = 3.0;  // More than 2.618
        double xad = 1.1;  // More than 1.0

        // Test for invalid Bat pattern
        boolean result = HarmonicPatterns.isBat(xab, abc, bcd, xad);

        // Assert that the result is false
        assertThat(result).isFalse();
    }

    @Test
    void testIsGartley_ValidGartleyPattern() {
        // Valid Gartley values
        double xab = 0.7;
        double abc = 0.5;
        double bcd = 1.5;
        double xad = 0.7;

        // Test for valid Gartley pattern
        boolean result = HarmonicPatterns.isGartley(xab, abc, bcd, xad);

        // Assert that the result is true
        assertThat(result).isTrue();
    }

    @Test
    void testIsGartley_InvalidGartleyPattern() {
        // Invalid Gartley values
        double xab = 0.5;  // Less than 0.618
        double abc = 0.9;  // More than 0.886
        double bcd = 1.7;  // More than 1.618
        double xad = 0.5;  // Less than 0.618

        // Test for invalid Gartley pattern
        boolean result = HarmonicPatterns.isGartley(xab, abc, bcd, xad);

        // Assert that the result is false
        assertThat(result).isFalse();
    }

    @Test
    void testDetectABCDPattern_ValidABCDPattern() {
        // Setting up data for a valid ABCD pattern
        x.setClosePrice("1.000");
        a.setClosePrice("1.200");
        b.setClosePrice("1.100");
        c.setClosePrice("1.150");
        d.setClosePrice("1.050");

        // Test the detection of an ABCD pattern
        boolean result = HarmonicPatterns.detectABCDPattern(x, a, b, c, d);

        // Assert that the result is true (ABCD pattern detected)
        assertThat(result).isTrue();
    }

    @Test
    void testDetectABCDPattern_NoPatternDetected() {
        // Setting up data for no valid pattern
        x.setClosePrice("1.000");
        a.setClosePrice("1.200");
        b.setClosePrice("1.500");  // Big difference making no valid pattern
        c.setClosePrice("1.400");
        d.setClosePrice("1.350");

        // Test the detection of any pattern
        boolean result = HarmonicPatterns.detectABCDPattern(x, a, b, c, d);

        // Assert that the result is false (no pattern detected)
        assertThat(result).isFalse();
    }
}

