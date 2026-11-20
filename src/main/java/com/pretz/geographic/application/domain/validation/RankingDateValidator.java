package com.pretz.geographic.application.domain.validation;

import java.time.LocalDate;

public class RankingDateValidator {
    private static final String INVALID_DATE_RANKING_MESSAGE = "Final daily ranking is available only for past dates while input date %s doesn't fulfill this condition";
    private static final String INVALID_INTERVAL_MESSAGE = "Start date %s of the interval must not be after its end date %s";

    //TODO [GEOG-15] Fix the timezone issue
    public void validatePastInterval(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidDateException(String.format(INVALID_INTERVAL_MESSAGE, from, to));
        }
        validatePastDate(to);
    }

    public void validatePastDate(LocalDate date) {
        if (!date.isBefore(LocalDate.now())) {
            throw new InvalidDateException(String.format(INVALID_DATE_RANKING_MESSAGE, date));
        }
    }
}
