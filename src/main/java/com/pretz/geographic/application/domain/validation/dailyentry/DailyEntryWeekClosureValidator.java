package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.service.ValidationIntermediateResult;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.WEEK_CLOSED;

public class DailyEntryWeekClosureValidator implements DailyEntryContextualValidator{

    @Override
    public ValidationIntermediateResult validate(ValidationIntermediateResult validationIntermediateResult,
                                                 ContextualLookups contextualLookups) {

        var calculatedWeeks = contextualLookups.gameWeeks();
        var result = validationIntermediateResult.entriesPassed().stream().collect(Collectors.partitioningBy(
                it -> !calculatedWeeks.contains(new GameWeek(it.game().gameId(), it.getWeek()))));
        var successes = result.get(true);
        var newFailures = result.get(false).stream().map(it -> new AddDailyEntryFailure(new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(it.game().gameId().id(), it.game().name()),
                new AddDailyEntryCommand.PlayerRef(it.player().playerId().id(), it.player().name()),
                it.date(),
                it.points(),
                it.submittedAt()), List.of(WEEK_CLOSED)));
        return new ValidationIntermediateResult(successes, Stream.concat(validationIntermediateResult.failures().stream(), newFailures).toList());
    }
}
