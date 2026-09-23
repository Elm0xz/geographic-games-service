package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;

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

public class DailyEntryReferenceValidator {

    private final LoadGamePort loadGamePort;
    private final LoadPlayerPort loadPlayerPort;

    public DailyEntryReferenceValidator(LoadGamePort loadGamePort, LoadPlayerPort loadPlayerPort) {
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
    }

    public ValidationIntermediateResult validate(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var gameLookup = loadGamePort.loadGames(addDailyEntryCommands.stream().map(it -> it.game().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.gameId().id(), Function.identity()));
        var playerLookup = loadPlayerPort.loadPlayers(addDailyEntryCommands.stream().map(it -> it.player().id()).toList())
                .stream().collect(Collectors.toMap(it -> it.playerId().id(), Function.identity()));

        var validatedCommands = addDailyEntryCommands.stream()
                .map(it -> new ValidatedCommand(it, List.<AddDailyEntryFailure.Reason>of()))
                .map(this::validateDate)
                .map(it -> validateGame(it, gameLookup))
                .map(it -> validatePlayer(it, playerLookup))
                .toList();

        return toIntermediateResult(validatedCommands, gameLookup, playerLookup);
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

    private ValidationIntermediateResult toIntermediateResult(List<ValidatedCommand> commands,
                                                              Map<Long, Game> gameLookup,
                                                              Map<Long, Player> playerLookup) {
        var validationResults = commands.stream()
                .collect(Collectors.partitioningBy(vc -> vc.rejectionResults().isEmpty()));
        return new ValidationIntermediateResult(
                validationResults.get(true).stream().map(vc -> lookupEntry(gameLookup, playerLookup, vc.command())).toList(),
                validationResults.get(false).stream().map(DailyEntryReferenceValidator::toFailure).toList());
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

    private record ValidatedCommand(AddDailyEntryCommand command,
                                    List<AddDailyEntryFailure.Reason> rejectionResults) {
    }
}
