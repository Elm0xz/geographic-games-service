package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;

import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_GAME;

public class DailyEntryGameValidator implements DailyEntryReferenceValidator {

    @Override
    public ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups) {
        var command = vc.command();
        if (isGameIdUnknown(lookups, command) || isGameNameUnknown(lookups, command)) {
            return new ValidatedCommand(command, Stream.concat(vc.rejectionResults().stream(), Stream.of(UNKNOWN_GAME)).toList());
        } else return vc;
    }

    private boolean isGameIdUnknown(ReferenceLookups lookups, AddDailyEntryCommand command) {
        return !lookups.games().containsKey(command.game().id());
    }

    private boolean isGameNameUnknown(ReferenceLookups lookups, AddDailyEntryCommand command) {
        return !command.game().name().equals(lookups.games().get(command.game().id()).name());
    }
}
