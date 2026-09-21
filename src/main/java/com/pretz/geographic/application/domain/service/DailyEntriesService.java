package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.INVALID_DATE;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.SUPERSEDED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_GAME;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_PLAYER;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.WEEK_CLOSED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.CORRECTED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.NEW;
import static java.time.ZoneOffset.UTC;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private final SaveDailyEntryPort saveDailyEntryPort;
    private final LoadGamePort loadGamePort;
    private final LoadPlayerPort loadPlayerPort;
    private final LoadWeeklyRankingPort loadWeeklyRankingPort;
    private final LoadDailyEntriesPort loadDailyEntriesPort;

    private final GameNameValidator gameNameValidator;
    private final PlayerNameValidator playerNameValidator;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort,
                               LoadGamePort loadGamePort,
                               LoadPlayerPort loadPlayerPort,
                               LoadWeeklyRankingPort loadWeeklyRankingPort,
                               LoadDailyEntriesPort loadDailyEntriesPort,
                               GameNameValidator gameNameValidator,
                               PlayerNameValidator playerNameValidator) {
        this.saveDailyEntryPort = saveDailyEntryPort;
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
        this.loadWeeklyRankingPort = loadWeeklyRankingPort;
        this.loadDailyEntriesPort = loadDailyEntriesPort;
        this.gameNameValidator = gameNameValidator;
        this.playerNameValidator = playerNameValidator;
    }

    @Override
    public DailyEntry addDailyEntry(AddDailyEntryCommand command) {

        var game = loadAndValidateGame(command);
        var player = loadAndValidatePlayer(command);

        return saveDailyEntryPort.save(new DailyEntry(null, game, command.date(), player, command.points()));
    }


    @Override
    public AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var intermediateResult = validateTierOne(addDailyEntryCommands);
        var afterTierTwoResult = validateTierTwo(intermediateResult);

        var saved = saveDailyEntryPort.saveAll(afterTierTwoResult.successList().stream().map(AddDailyEntrySuccess::entry).toList());
        var successList = IntStream.range(0, saved.size())
                .mapToObj(i -> new AddDailyEntrySuccess(saved.get(i), afterTierTwoResult.successList().get(i).successCode()))
                .toList();
        return new AddDailyEntriesResult(successList, afterTierTwoResult.failureList());
    }

    private ValidationIntermediateResult validateTierOne(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var gameLookup = loadGamePort.loadGames(addDailyEntryCommands.stream().map(it -> it.game().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.gameId().id(), Function.identity()));
        var playerLookup = loadPlayerPort.loadPlayers(addDailyEntryCommands.stream().map(it -> it.player().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.playerId().id(), Function.identity()));

        var tier1Validation = addDailyEntryCommands.stream()
                .map(it -> new ValidatedCommand(it, List.of()))
                .map(it -> validateDate(it))
                .map(it -> validateGame(it, gameLookup))
                .map(it -> validatePlayer(it, playerLookup))
                .toList();

        return ValidationIntermediateResult.from(tier1Validation, gameLookup, playerLookup);
    }

    private AddDailyEntriesResult validateTierTwo(ValidationIntermediateResult intermediateResult) {
        var calculatedWeeks = loadWeeklyRankingPort.loadCalculatedWeeks(intermediateResult.entriesPassed.stream()
                .map(it -> new GameWeek(it.game().gameId(), it.getWeek())).toList());
        var dbPresentEntries = loadDailyEntriesPort.loadEntries(intermediateResult.entriesPassed().stream()
                .map(it -> new DailyRankingService.GameAndDate(it.game(), it.date())).toList());

        var afterWeekValResult = validateWeek(intermediateResult, calculatedWeeks);
        return validateDuplicates(afterWeekValResult, dbPresentEntries);
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
        return new AddDailyEntrySuccess(new DailyEntry(timestampIndex.dbId, rawEntry.game(), rawEntry.date(), rawEntry.player(), rawEntry.points(), rawEntry.submittedAt()), timestampIndex.successType());
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

    private ValidatedCommand validateDate(ValidatedCommand step) {
        var command = step.command();
        //TODO implement offset
        if (command.date().isAfter(LocalDate.now()) || (!LocalDate.ofInstant(command.submittedAt(), UTC).equals(command.date()))) {
            return new ValidatedCommand(command, Stream.concat(step.rejectionResults().stream(), Stream.of(INVALID_DATE)).toList());
        } else return step;
    }

    private ValidatedCommand validateGame(ValidatedCommand step, Map<Long, Game> gameLookup) {
        var command = step.command();
        if (!gameLookup.containsKey(command.game().id()) || !command.game().name().equals(gameLookup.get(command.game().id()).name())) {
            return new ValidatedCommand(command, Stream.concat(step.rejectionResults().stream(), Stream.of(UNKNOWN_GAME)).toList());
        } else return step;
    }

    private ValidatedCommand validatePlayer(ValidatedCommand step, Map<Long, Player> playerLookup) {
        var command = step.command();
        if (!playerLookup.containsKey(command.player().id()) || !command.player().name().equals(playerLookup.get(command.player().id()).name())) {
            return new ValidatedCommand(command, Stream.concat(step.rejectionResults().stream(), Stream.of(UNKNOWN_PLAYER)).toList());
        } else return step;
    }

    private record ValidatedCommand(AddDailyEntryCommand command,
                                    List<AddDailyEntryFailure.Reason> rejectionResults) {
    }

    private record ValidationIntermediateResult(List<DailyEntry> entriesPassed,
                                                List<AddDailyEntryFailure> failures) {

        static ValidationIntermediateResult from(List<ValidatedCommand> commands,
                                                 Map<Long, Game> gameLookup,
                                                 Map<Long, Player> playerLookup) {
            var validationResults = commands.stream()
                    .collect(Collectors.partitioningBy(vc -> vc.rejectionResults().isEmpty()));
            return new ValidationIntermediateResult(
                    validationResults.get(true).stream().map(vc -> lookupEntry(gameLookup, playerLookup, vc.command())).toList(),
                    validationResults.get(false).stream().map(ValidationIntermediateResult::toFailure).toList());
        }

        private static DailyEntry lookupEntry(Map<Long, Game> gameLookup,
                                              Map<Long, Player> playerLookup,
                                              AddDailyEntryCommand command) {
            return new DailyEntry(null,
                    gameLookup.get(command.game().id()), command.date(),
                    playerLookup.get(command.player().id()), command.points(),
                    command.submittedAt());
        }

        private static AddDailyEntryFailure toFailure(ValidatedCommand vc) {
            return new AddDailyEntryFailure(vc.command(), vc.rejectionResults());
        }
    }

    private record TimestampIndex(int index, Instant timestamp, AddDailyEntrySuccess.DailyEntrySuccess successType,
                                  DailyEntryId dbId) {
    }

    private record GamePlayerDate(Game game, Player player, LocalDate date) {
    }

    private Game loadAndValidateGame(AddDailyEntryCommand command) {
        var game = loadGamePort.loadGame(command.game().id());
        gameNameValidator.validate(command.game().name(), game.name());
        return game;
    }

    private Player loadAndValidatePlayer(AddDailyEntryCommand command) {
        var player = loadPlayerPort.loadPlayer(command.player().id());
        playerNameValidator.validate(command.player().name(), player.name());
        return player;
    }
}
