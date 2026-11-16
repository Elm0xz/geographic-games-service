package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;

import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_PLAYER;

public class DailyEntryPlayerValidator implements DailyEntryReferenceValidator {

    @Override
    public ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups) {
        var command = vc.command();
        if (isPlayerIdUnknown(lookups, command) || isPlayerNameUnknown(lookups, command)) {
            return new ValidatedCommand(command, Stream.concat(vc.rejectionResults().stream(), Stream.of(UNKNOWN_PLAYER)).toList());
        } else return vc;
    }

    private boolean isPlayerIdUnknown(ReferenceLookups lookups, AddDailyEntryCommand command) {
        return !lookups.players().containsKey(command.player().id());
    }

    private boolean isPlayerNameUnknown(ReferenceLookups lookups, AddDailyEntryCommand command) {
        return !command.player().name().equals(lookups.players().get(command.player().id()).name());
    }
}
