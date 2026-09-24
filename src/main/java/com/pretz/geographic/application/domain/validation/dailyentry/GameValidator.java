package com.pretz.geographic.application.domain.validation.dailyentry;

import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_GAME;

public class GameValidator implements DailyEntryReferenceValidator {

    @Override
    public ValidatedCommand validate(ValidatedCommand step, ReferenceLookups lookups) {
        var command = step.command();
        if (!lookups.games().containsKey(command.game().id()) || !command.game().name().equals(lookups.games().get(command.game().id()).name())) {
            return new ValidatedCommand(command, Stream.concat(step.rejectionResults().stream(), Stream.of(UNKNOWN_GAME)).toList());
        } else return step;
    }
}
