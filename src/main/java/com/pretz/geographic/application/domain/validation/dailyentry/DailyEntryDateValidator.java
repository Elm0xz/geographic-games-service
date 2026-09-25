package com.pretz.geographic.application.domain.validation.dailyentry;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.INVALID_DATE;
import static java.time.ZoneOffset.UTC;

public class DailyEntryDateValidator implements DailyEntryReferenceValidator {

    @Override
    public ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups) {
        var command = vc.command();
        //TODO implement offset
        if (command.date().isAfter(LocalDate.now()) || (!LocalDate.ofInstant(command.submittedAt(), UTC).equals(command.date()))) {
            return new ValidatedCommand(command, Stream.concat(vc.rejectionResults().stream(), Stream.of(INVALID_DATE)).toList());
        } else return vc;
    }
}
