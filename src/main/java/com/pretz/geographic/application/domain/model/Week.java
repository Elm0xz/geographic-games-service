package com.pretz.geographic.application.domain.model;

import com.pretz.geographic.application.domain.validation.InvalidDateException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Objects;

public final class Week {

    private static final DateTimeFormatter ISO_WEEK_STRICT =
            DateTimeFormatter.ISO_WEEK_DATE.withResolverStyle(ResolverStyle.STRICT);

    private final int year;
    private final int week;

    private Week(int year, int week) {
        this.year = year;
        this.week = week;
    }

    public static Week of(int year, int number) {
        try {
            LocalDate date = LocalDate.parse(
                    String.format("%04d-W%02d-1", year, number),
                    ISO_WEEK_STRICT
            );
            return new Week(
                    date.get(IsoFields.WEEK_BASED_YEAR),
                    date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            );
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Invalid week number: " + number);
        }
    }

    public LocalDate monday() {
        return LocalDate.now()
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(ChronoField.DAY_OF_WEEK, 1);
    }

    public LocalDate sunday() {
        return monday().plusDays(6);
    }

    public int year() {
        return year;
    }

    public int week() {
        return week;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Week) obj;
        return this.year == that.year &&
                this.week == that.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, week);
    }

    @Override
    public String toString() {
        return "Week[" +
                "year=" + year + ", " +
                "number=" + week + ']';
    }
}
