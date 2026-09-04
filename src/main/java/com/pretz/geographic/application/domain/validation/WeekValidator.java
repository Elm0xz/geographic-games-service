package com.pretz.geographic.application.domain.validation;

import com.pretz.geographic.application.domain.model.Week;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class WeekValidator {

    private static final String INVALID_YEAR_RANKING_MESSAGE = "Weekly ranking is available only for past dates while input year %s doesn't fulfill this condition";
    private static final String INVALID_WEEK_RANKING_MESSAGE = "Weekly ranking is available only for past dates while input week %s doesn't fulfill this condition";

    public void validate(Week week) {

        if (week.year() > LocalDate.now().getYear())
            throw new InvalidDateException(String.format(INVALID_YEAR_RANKING_MESSAGE, week.year()));
        else if (week.number() >= LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear()))
            throw new InvalidDateException(String.format(INVALID_WEEK_RANKING_MESSAGE, week.year()));
    }
}
