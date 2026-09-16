package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.INVALID_DATE;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_GAME;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_PLAYER;
import static java.time.ZoneOffset.UTC;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private final SaveDailyEntryPort saveDailyEntryPort;
    private final LoadGamePort loadGamePort;
    private final LoadPlayerPort loadPlayerPort;
    private final GameNameValidator gameNameValidator;
    private final PlayerNameValidator playerNameValidator;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort,
                               LoadGamePort loadGamePort,
                               LoadPlayerPort loadPlayerPort,
                               GameNameValidator gameNameValidator,
                               PlayerNameValidator playerNameValidator) {
        this.saveDailyEntryPort = saveDailyEntryPort;
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
        this.gameNameValidator = gameNameValidator;
        this.playerNameValidator = playerNameValidator;
    }

    @Override
    public DailyEntry addDailyEntry(AddDailyEntryCommand command) {

        var game = loadAndValidateGame(command);
        var player = loadAndValidatePlayer(command);

        return saveDailyEntryPort.save(new DailyEntry(null, game, command.date(), player, command.points()));
    }

    //TODO [GEOG-11] implement
    @Override
    public AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {
        var gameLookup = loadGamePort.loadGames(addDailyEntryCommands.stream().map(it -> it.game().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.gameId().id(), Function.identity()));
        var playerLookup = loadPlayerPort.loadPlayers(addDailyEntryCommands.stream().map(it -> it.player().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.playerId().id(), Function.identity()));

        var intermediateResult = validateDateAndGameAndPlayers(addDailyEntryCommands, gameLookup, playerLookup);
        var finalResult = validateTierTwo(intermediateResult);
        //TODO 3. [GEOG-11] tier two - validate week not closed, data against DB, detect duplicates, update/insert if needed (transaction)
        return new AddDailyEntriesResult();
    }

    private ValidationIntermediateResult validateDateAndGameAndPlayers(List<AddDailyEntryCommand> addDailyEntryCommands,
                                                                       Map<Long, Game> gameLookup,
                                                                       Map<Long, Player> playerLookup) {
        var tier1Validation = addDailyEntryCommands.stream()
                .map(it -> new SingleCommandValidationStep(it, List.of()))
                .map(it -> validateDate(it))
                .map(it -> validateGame(it, gameLookup))
                .map(it -> validatePlayer(it, playerLookup))
                .toList();

        return new ValidationIntermediateResult(tier1Validation, gameLookup, playerLookup);
    }

    private AddDailyEntriesResult validateTierTwo(ValidationIntermediateResult intermediateResult) {
        return null;
    }

    private SingleCommandValidationStep validateDate(SingleCommandValidationStep step) {
        var command = step.command();
        //TODO implement offset
        if (command.date().isAfter(LocalDate.now()) || (!LocalDate.ofInstant(command.submittedAt(), UTC).equals(command.date()))) {
            return new SingleCommandValidationStep(command, Stream.concat(step.reasons().stream(), Stream.of(INVALID_DATE)).toList());
        } else return step;
    }

    private SingleCommandValidationStep validateGame(SingleCommandValidationStep step, Map<Long, Game> gameLookup) {
        var command = step.command();
        if (!gameLookup.containsKey(command.game().id()) || !command.game().name().equals(gameLookup.get(command.game().id()).name())) {
            return new SingleCommandValidationStep(command, Stream.concat(step.reasons().stream(), Stream.of(UNKNOWN_GAME)).toList());
        } else return step;
    }

    private SingleCommandValidationStep validatePlayer(SingleCommandValidationStep step, Map<Long, Player> playerLookup) {
        var command = step.command();
        if (!playerLookup.containsKey(command.player().id()) || !command.player().name().equals(playerLookup.get(command.player().id()).name())) {
            return new SingleCommandValidationStep(command, Stream.concat(step.reasons().stream(), Stream.of(UNKNOWN_PLAYER)).toList());
        } else return step;
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

    private record SingleCommandValidationStep(AddDailyEntryCommand command,
                                               List<AddDailyEntryFailure.Reason> reasons) {
    }

    private record ValidationIntermediateResult(List<DailyEntry> entriesPassed,
                                                List<AddDailyEntryFailure> failures) {

        ValidationIntermediateResult(List<SingleCommandValidationStep> steps,
                                     Map<Long, Game> gameLookup,
                                     Map<Long, Player> playerLookup) {
            var validatedEntries = steps.stream()
                    .filter(it -> it.reasons().isEmpty())
                    .map(it -> new DailyEntry(null,
                            gameLookup.get(it.command.game().id()), it.command.date(),
                            playerLookup.get(it.command.player().id()), it.command.points(),
                            it.command().submittedAt()))
                    .toList();
            var failedCommands = steps.stream()
                    .filter(it -> !it.reasons().isEmpty())
                    .map(it -> new AddDailyEntryFailure(it.command(), it.reasons()))
                    .toList();
            this(validatedEntries, failedCommands);
        }
    }
}
