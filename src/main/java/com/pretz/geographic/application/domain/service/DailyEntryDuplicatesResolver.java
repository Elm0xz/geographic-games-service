package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.SUPERSEDED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.CORRECTED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.NEW;

public class DailyEntryDuplicatesResolver {

    private final LoadDailyEntriesPort loadDailyEntriesPort;

    public DailyEntryDuplicatesResolver(LoadDailyEntriesPort loadDailyEntriesPort) {
        this.loadDailyEntriesPort = loadDailyEntriesPort;
    }

    AddDailyEntriesResult validateDuplicates(ValidationIntermediateResult intermediateResult) {

        var arrivingEntries = intermediateResult.entriesPassed();

        var resolutionResult = resolveEntryDuplicates(arrivingEntries);

        return new AddDailyEntriesResult(resolutionResult.successes(),
                Stream.concat(intermediateResult.failures().stream(), resolutionResult.detectedFailures().stream()).toList());
    }

    private ResolutionResult resolveEntryDuplicates(List<DailyEntry> arrivingEntries) {

        var resolutionStateMap = getPersistedEntries(arrivingEntries).stream()
                .collect(Collectors.toMap(
                        it -> new GamePlayerDate(it.game(), it.player(), it.date()),
                        it -> new EntryResolutionState(-1, it.submittedAt(), null, it.dailyEntryId())));

        List<AddDailyEntryFailure> detectedFailures = new ArrayList<AddDailyEntryFailure>();

        for (int i = 0; i < arrivingEntries.size(); i++) {
            var arriving = arrivingEntries.get(i);
            GamePlayerDate key = new GamePlayerDate(arriving.game(), arriving.player(), arriving.date());
            if (resolutionStateMap.containsKey(key)) {
                EntryResolutionState incumbent = resolutionStateMap.get(key);
                if (arriving.submittedAt().isBefore(incumbent.submittedAt())) {
                    if (isNotAlreadyPersisted(incumbent)) {
                        addToFailures(detectedFailures, arrivingEntries.get(incumbent.batchId()));
                        resolutionStateMap.put(key, new EntryResolutionState(i, arriving.submittedAt(), incumbent.successType(), incumbent.dbId()));
                    } else {
                        resolutionStateMap.put(key, new EntryResolutionState(i, arriving.submittedAt(), CORRECTED, incumbent.dbId()));
                    }

                } else {
                    addToFailures(detectedFailures, arriving);
                }
            } else {
                resolutionStateMap.put(key, new EntryResolutionState(i, arriving.submittedAt(), NEW, null));
            }
        }

        return new ResolutionResult(prepareEntriesToSave(resolutionStateMap, arrivingEntries), detectedFailures);
    }

    private List<DailyEntry> getPersistedEntries(List<DailyEntry> arrivingEntries) {
        return loadDailyEntriesPort.loadEntries(arrivingEntries.stream()
                .map(it -> new DailyRankingService.GameAndDate(it.game(), it.date())).toList());
    }

    private void addToFailures(List<AddDailyEntryFailure> detectedFailures, DailyEntry entry) {
        detectedFailures.add(new AddDailyEntryFailure(new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(entry.game().gameId().id(), entry.game().name()),
                new AddDailyEntryCommand.PlayerRef(entry.player().playerId().id(), entry.player().name()),
                entry.date(),
                entry.points(),
                entry.submittedAt()), List.of(SUPERSEDED)));
    }

    private List<AddDailyEntrySuccess> prepareEntriesToSave(Map<GamePlayerDate, EntryResolutionState> entriesAndTimestamps,
                                                            List<DailyEntry> entries) {
        var entryIdsToSave = entriesAndTimestamps.values().stream()
                .filter(this::isNotAlreadyPersisted)
                .sorted(Comparator.comparingInt(EntryResolutionState::batchId))
                .toList();
        return entryIdsToSave.stream()
                .map(it -> buildEntryToSave(it, entries))
                .toList();
    }

    private boolean isNotAlreadyPersisted(EntryResolutionState state) {
        return state.batchId() != -1;
    }

    private AddDailyEntrySuccess buildEntryToSave(EntryResolutionState entryResolutionState, List<DailyEntry> entriesToCheck) {

        DailyEntry rawEntry = entriesToCheck.get(entryResolutionState.batchId());
        return new AddDailyEntrySuccess(
                new DailyEntry(entryResolutionState.dbId(),
                        rawEntry.game(),
                        rawEntry.date(),
                        rawEntry.player(),
                        rawEntry.points(),
                        rawEntry.submittedAt()),
                entryResolutionState.successType());
    }

    private record ResolutionResult(List<AddDailyEntrySuccess> successes,
                                    List<AddDailyEntryFailure> detectedFailures) {
    }

    private record EntryResolutionState(int batchId, Instant submittedAt,
                                        AddDailyEntrySuccess.DailyEntrySuccess successType,
                                        DailyEntryId dbId) {
    }

    private record GamePlayerDate(Game game, Player player, LocalDate date) {
    }
}
