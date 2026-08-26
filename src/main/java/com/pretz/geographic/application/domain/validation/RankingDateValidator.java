package com.pretz.geographic.application.domain.validation;

import java.time.LocalDate;

public class RankingDateValidator {
    private static final String INVALID_DATE_RANKING_MESSAGE = "Final daily ranking is available only for past dates while input date %s doesn't fulfill this condition";

    //TODO [GEOG-15] Fix the timezone issue
    public void validatePastDate(LocalDate date) {
        if (!date.isBefore(LocalDate.now())) {
            throw new InvalidDateException(String.format(INVALID_DATE_RANKING_MESSAGE, date));
        }
    }
}
