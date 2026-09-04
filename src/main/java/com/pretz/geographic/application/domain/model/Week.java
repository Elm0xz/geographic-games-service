package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public record Week(int year, int number) {

    public LocalDate monday() {
        return LocalDate.now()
                .with(WeekFields.ISO.weekBasedYear(), year)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), number)
                .with(WeekFields.ISO.dayOfWeek(), 1);
    }

    public LocalDate sunday() {
        return monday().plusDays(6);
    }
}
