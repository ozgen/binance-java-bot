package com.ozgen.binancebot.utils;


import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFactoryTest {

    @Test
    public void testGetDateBeforeInMonths() {
        // Set a fixed instant for testing purposes
        Instant fixedInstant = Instant.parse("2024-10-01T00:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        // Create a DateFactory instance with the fixed clock
        DateFactory dateFactory = new DateFactory(fixedClock);

        int monthsBefore = 3;
        Date result = dateFactory.getDateBeforeInMonths(monthsBefore);

        // Calculate the expected date
        LocalDate expectedLocalDate = LocalDate.now(fixedClock).minusMonths(monthsBefore);
        Date expectedDate = Date.from(expectedLocalDate.atStartOfDay(zoneId).toInstant());

        // Assert that the year, month, and day are as expected
        LocalDate resultLocalDate = result.toInstant().atZone(zoneId).toLocalDate();
        assertEquals(expectedLocalDate.getYear(), resultLocalDate.getYear());
        assertEquals(expectedLocalDate.getMonth(), resultLocalDate.getMonth());
        assertEquals(expectedLocalDate.getDayOfMonth(), resultLocalDate.getDayOfMonth());
    }
}

