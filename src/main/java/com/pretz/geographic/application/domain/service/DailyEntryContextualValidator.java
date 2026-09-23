package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.SUPERSEDED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.WEEK_CLOSED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.CORRECTED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.NEW;

public class DailyEntryContextualValidator {

    private final LoadWeeklyRankingPort loadWeeklyRankingPort;
    private final LoadDailyEntriesPort loadDailyEntriesPort;

    public DailyEntryContextualValidator(LoadWeeklyRankingPort loadWeeklyRankingPort,
                                         LoadDailyEntriesPort loadDailyEntriesPort) {
        this.loadWeeklyRankingPort = loadWeeklyRankingPort;
        this.loadDailyEntriesPort = loadDailyEntriesPort;
    }

    public AddDailyEntriesResult validate(ValidationIntermediateResult intermediateResult) {
        var calculatedWeeks = loadWeeklyRankingPort.loadCalculatedWeeks(intermediateResult.entriesPassed().stream()
                .map(it -> new GameWeek(it.game().gameId(), it.getWeek())).toList());
        var dbPresentEntries = loadDailyEntriesPort.loadEntries(intermediateResult.entriesPassed().stream()
                .map(it -> new DailyRankingService.GameAndDate(it.game(), it.date())).toList());

        var afterWeekValResult = validateWeek(intermediateResult, calculatedWeeks);
        return validateDuplicates(afterWeekValResult, dbPresentEntries);
    }

    private ValidationIntermediateResult validateWeek(ValidationIntermediateResult validationIntermediateResult,
                                                      Set<GameWeek> calculatedWeeks) {
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

    private AddDailyEntriesResult validateDuplicates(ValidationIntermediateResult intermediateResult,
                                                     List<DailyEntry> dbPresentEntries) {

        var referenceMap = dbPresentEntries.stream()
                .collect(Collectors.toMap(
                        it -> new GamePlayerDate(it.game(), it.player(), it.date()),
                        it -> new TimestampIndex(-1, it.submittedAt(), null, it.dailyEntryId())));

        var entriesToCheck = intermediateResult.entriesPassed();

        List<AddDailyEntryFailure> failuresToAdd = new ArrayList<>();
        for (int i = 0; i < entriesToCheck.size(); i++) {
            var entry = entriesToCheck.get(i);
            GamePlayerDate key = new GamePlayerDate(entry.game(), entry.player(), entry.date());
            if (referenceMap.containsKey(key)) {
                TimestampIndex incumbent = referenceMap.get(key);
                if (entry.submittedAt().isBefore(incumbent.timestamp())) {
                    if (incumbent.index() != -1) {
                        var entryToPurge = entriesToCheck.get(incumbent.index());
                        failuresToAdd.add(new AddDailyEntryFailure(new AddDailyEntryCommand(
                                new AddDailyEntryCommand.GameRef(entryToPurge.game().gameId().id(), entryToPurge.game().name()),
                                new AddDailyEntryCommand.PlayerRef(entryToPurge.player().playerId().id(), entryToPurge.player().name()),
                                entryToPurge.date(),
                                entryToPurge.points(),
                                entryToPurge.submittedAt()), List.of(SUPERSEDED)));
                        referenceMap.put(key, new TimestampIndex(i, entry.submittedAt(), incumbent.successType(), incumbent.dbId()));
                    } else {
                        referenceMap.put(key, new TimestampIndex(i, entry.submittedAt(), CORRECTED, incumbent.dbId()));
                    }

                } else {
                    failuresToAdd.add(new AddDailyEntryFailure(new AddDailyEntryCommand(
                            new AddDailyEntryCommand.GameRef(entry.game().gameId().id(), entry.game().name()),
                            new AddDailyEntryCommand.PlayerRef(entry.player().playerId().id(), entry.player().name()),
                            entry.date(),
                            entry.points(),
                            entry.submittedAt()), List.of(SUPERSEDED)));
                }
            } else {
                referenceMap.put(key, new TimestampIndex(i, entry.submittedAt(), NEW, null));
            }
        }
        var successfulEntriesIds = referenceMap.values().stream()
                .filter(it -> it.index() != -1)
                .sorted(Comparator.comparingInt(TimestampIndex::index))
                .toList();
        var successfulEntries = successfulEntriesIds.stream()
                .map(it -> buildEntryToSave(it, entriesToCheck))
                .toList();

        return new AddDailyEntriesResult(successfulEntries,
                Stream.concat(intermediateResult.failures().stream(), failuresToAdd.stream()).toList());
    }

    private AddDailyEntrySuccess buildEntryToSave(TimestampIndex timestampIndex, List<DailyEntry> entriesToCheck) {

        DailyEntry rawEntry = entriesToCheck.get(timestampIndex.index());
        return new AddDailyEntrySuccess(new DailyEntry(timestampIndex.dbId(), rawEntry.game(), rawEntry.date(), rawEntry.player(), rawEntry.points(), rawEntry.submittedAt()), timestampIndex.successType());
    }

    private record TimestampIndex(int index, Instant timestamp, AddDailyEntrySuccess.DailyEntrySuccess successType,
                                  DailyEntryId dbId) {
    }

    private record GamePlayerDate(Game game, Player player, LocalDate date) {
    }
}
