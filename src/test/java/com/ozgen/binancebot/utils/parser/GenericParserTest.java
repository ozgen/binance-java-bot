package com.ozgen.binancebot.utils.parser;


import com.ozgen.binancebot.model.binance.AssetBalance;
import com.ozgen.binancebot.utils.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class GenericParserTest {
    @Test
    public void testGetDouble_withValidNumber() {
        String validNumber = "123.45";
        Double result = GenericParser.getDouble(validNumber);
        assertEquals(123.45, result, 0.0);
    }

    @Test
    public void testGetDouble_withInvalidNumber() {
        String invalidNumber = "abc";
        Double result = GenericParser.getDouble(invalidNumber);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testGetDouble_withNull() {
        String nullString = null;
        Double result = GenericParser.getDouble(nullString);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void testGetFormattedDoubleWithNormalValues() {
        assertEquals(123.0, GenericParser.getFormattedDouble(123.4567), "Normal value should be formatted correctly.");
        assertEquals(0.1234, GenericParser.getFormattedDouble(0.123456789), "Value should be rounded to 6 decimal places.");
    }

    @Test
    public void testGetFormattedDoubleWithZero() {
        assertEquals(0.0, GenericParser.getFormattedDouble(0.0), "Zero should be formatted correctly.");
    }

    @Test
    public void testGetFormattedDoubleWithVerySmallValue() {
        assertEquals(0.00000001, GenericParser.getFormattedDouble(0.00000001), "Very small value should be formatted correctly.");
    }

    @Test
    public void testGetFormattedDoubleWithVeryLargeValue() {
        assertEquals(12345678.0, GenericParser.getFormattedDouble(12345678.0), "Very large value should be formatted correctly.");
    }

    @Test
    public void testGetFormattedDoubleWithNull() {
        assertEquals(0.0, GenericParser.getFormattedDouble(Double.parseDouble("0")), "Null should return 0.0.");
    }

    @Test
    public void testGetAssetFromSymbol() throws Exception {
        List<AssetBalance> assets = TestData.getAssets();
        assertEquals(0.00004272, GenericParser.getAssetFromSymbol(assets, "BNB"));
    }
}
