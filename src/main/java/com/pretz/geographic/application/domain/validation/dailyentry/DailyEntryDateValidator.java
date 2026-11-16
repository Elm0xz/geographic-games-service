package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.INVALID_DATE;
import static java.time.ZoneOffset.UTC;

public class DailyEntryDateValidator implements DailyEntryReferenceValidator {

    @Override
    public ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups) {
        var command = vc.command();
        //TODO [GEOG-15] implement offset
        if (isFutureDate(command) || isTimestampFromDifferentDate(command)) {
            return new ValidatedCommand(command, Stream.concat(vc.rejectionResults().stream(), Stream.of(INVALID_DATE)).toList());
        } else return vc;
    }

    private boolean isFutureDate(AddDailyEntryCommand command) {
        return command.date().isAfter(LocalDate.now());
    }

    private boolean isTimestampFromDifferentDate(AddDailyEntryCommand command) {
        return !LocalDate.ofInstant(command.submittedAt(), UTC).equals(command.date());
    }
}
