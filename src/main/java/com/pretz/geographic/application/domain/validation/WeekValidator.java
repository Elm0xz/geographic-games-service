package com.pretz.geographic.application.domain.validation;

import com.pretz.geographic.application.domain.model.Week;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class WeekValidator {

    private static final String INVALID_RANKING_MESSAGE = "Weekly ranking is available only for past dates while input %s %s doesn't fulfill this condition";
    private static final String YEAR = "year";
    private static final String WEEK = "week";

    public void validate(Week week) {

        if (week.year() > LocalDate.now().getYear())
            throw new InvalidDateException(String.format(INVALID_RANKING_MESSAGE, YEAR, week.year()));
        else if (week.number() >= LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear()))
            throw new InvalidDateException(String.format(INVALID_RANKING_MESSAGE, WEEK, week.year()));
    }
}
