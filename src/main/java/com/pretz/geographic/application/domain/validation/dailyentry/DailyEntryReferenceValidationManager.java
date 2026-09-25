package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.service.ValidationIntermediateResult;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DailyEntryReferenceValidationManager {

    private final LoadGamePort loadGamePort;
    private final LoadPlayerPort loadPlayerPort;
    private final DailyEntryReferenceValidator validator;

    public DailyEntryReferenceValidationManager(LoadGamePort loadGamePort,
                                                LoadPlayerPort loadPlayerPort,
                                                DailyEntryReferenceValidator validator) {
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
        this.validator = validator;
    }

    public ValidationIntermediateResult validate(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var referenceLookups = new ReferenceLookups(
                loadGamePort.loadGames(addDailyEntryCommands.stream().map(it -> it.game().id()).toList())
                        .stream().collect(Collectors.toMap(it -> it.gameId().id(), Function.identity())),
                loadPlayerPort.loadPlayers(addDailyEntryCommands.stream().map(it -> it.player().id()).toList())
                        .stream().collect(Collectors.toMap(it -> it.playerId().id(), Function.identity())));


        var validatedCommands = addDailyEntryCommands.stream()
                .map(it -> new ValidatedCommand(it, List.<AddDailyEntryFailure.Reason>of()))
                .map(it -> validator.validate(it, referenceLookups))
                .toList();

        return toIntermediateResult(validatedCommands, referenceLookups);
    }

    private ValidationIntermediateResult toIntermediateResult(List<ValidatedCommand> commands,
                                                              ReferenceLookups lookups) {
        var validationResults = commands.stream()
                .collect(Collectors.partitioningBy(vc -> vc.rejectionResults().isEmpty()));
        return new ValidationIntermediateResult(
                validationResults.get(true).stream().map(vc -> lookupEntry(lookups.games(), lookups.players(), vc.command())).toList(),
                validationResults.get(false).stream().map(DailyEntryReferenceValidationManager::toFailure).toList());
    }

    private static AddDailyEntryFailure toFailure(ValidatedCommand vc) {
        return new AddDailyEntryFailure(vc.command(), vc.rejectionResults());
    }

    private static DailyEntry lookupEntry(Map<Long, Game> gameLookup,
                                          Map<Long, Player> playerLookup,
                                          AddDailyEntryCommand command) {
        return new DailyEntry(null,
                gameLookup.get(command.game().id()), command.date(),
                playerLookup.get(command.player().id()), command.points(),
                command.submittedAt());
    }
}
