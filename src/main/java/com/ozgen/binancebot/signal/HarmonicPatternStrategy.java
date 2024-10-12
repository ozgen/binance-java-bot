package com.ozgen.binancebot.signal;

import com.ozgen.binancebot.model.binance.KlineData;
import com.ozgen.binancebot.utils.parser.GenericParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HarmonicPatternStrategy {

    // Check for ABCD pattern
    public boolean isABCD(double abc, double bcd) {
        return (abc >= 0.382 && abc <= 0.886) && (bcd >= 1.13 && bcd <= 2.618);
    }

    // Check for Bat pattern (uses xab)
    public boolean isBat(double xab, double abc, double bcd, double xad) {
        return (xab >= 0.382 && xab <= 0.5) && (abc >= 0.382 && abc <= 0.886) &&
                (bcd >= 1.618 && bcd <= 2.618) && (xad <= 0.618 && xad <= 1.0);
    }

    // Check for Gartley pattern (uses xab)
    public boolean isGartley(double xab, double abc, double bcd, double xad) {
        return (xab >= 0.618 && xab <= 0.786) && (abc >= 0.382 && abc <= 0.886) &&
                (bcd >= 1.13 && bcd <= 1.618) && (xad >= 0.618 && xad <= 0.786);
    }

    // Function to calculate ratios and detect patterns
    public boolean detectABCDPattern(KlineData x, KlineData a, KlineData b, KlineData c, KlineData d) {

        double xab = Math.abs(GenericParser.getDouble(b.getClosePrice()) - GenericParser.getDouble(a.getClosePrice())) /
                Math.abs(GenericParser.getDouble(x.getClosePrice()) - GenericParser.getDouble(a.getClosePrice()));

        double abc = Math.abs(GenericParser.getDouble(b.getClosePrice()) - GenericParser.getDouble(c.getClosePrice())) /
                Math.abs(GenericParser.getDouble(a.getClosePrice()) - GenericParser.getDouble(b.getClosePrice()));

        double bcd = Math.abs(GenericParser.getDouble(c.getClosePrice()) - GenericParser.getDouble(d.getClosePrice())) /
                Math.abs(GenericParser.getDouble(b.getClosePrice()) - GenericParser.getDouble(c.getClosePrice()));

        double xad = Math.abs(GenericParser.getDouble(a.getClosePrice()) - GenericParser.getDouble(d.getClosePrice())) /
                Math.abs(GenericParser.getDouble(x.getClosePrice()) - GenericParser.getDouble(a.getClosePrice()));

        boolean abcdDetected = isABCD(abc, bcd);
        boolean batDetected = isBat(xab, abc, bcd, xad);
        boolean gartleyDetected = isGartley(xab, abc, bcd, xad);

        return abcdDetected || batDetected || gartleyDetected;
    }
}


