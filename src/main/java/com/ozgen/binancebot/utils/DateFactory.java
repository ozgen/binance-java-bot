package com.ozgen.binancebot.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
@AllArgsConstructor
public class DateFactory {

    private final Clock clock;



    public Date getDateBeforeInMonths(int monthsBefore) {
        LocalDate now = LocalDate.now(this.clock);
        LocalDate pastDate = now.minusMonths(monthsBefore);
        return Date.from(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
