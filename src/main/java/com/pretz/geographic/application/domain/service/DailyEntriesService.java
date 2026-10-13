package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryContextualValidationManager;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidationManager;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.util.List;
import java.util.stream.IntStream;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private final SaveDailyEntryPort saveDailyEntryPort;
    private final LoadGamePort loadGamePort;
    private final LoadPlayerPort loadPlayerPort;

    private final GameNameValidator gameNameValidator;
    private final PlayerNameValidator playerNameValidator;

    private final DailyEntryReferenceValidationManager referenceValidator;
    private final DailyEntryContextualValidationManager contextualValidator;
    private final DailyEntryDuplicatesResolver resolver;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort,
                               LoadGamePort loadGamePort,
                               LoadPlayerPort loadPlayerPort,
                               GameNameValidator gameNameValidator,
                               PlayerNameValidator playerNameValidator,
                               DailyEntryReferenceValidationManager referenceValidator,
                               DailyEntryContextualValidationManager contextualValidator,
                               DailyEntryDuplicatesResolver resolver) {
        this.saveDailyEntryPort = saveDailyEntryPort;
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
        this.gameNameValidator = gameNameValidator;
        this.playerNameValidator = playerNameValidator;
        this.referenceValidator = referenceValidator;
        this.contextualValidator = contextualValidator;
        this.resolver = resolver;
    }

    @Override
    public DailyEntry addDailyEntry(AddDailyEntryCommand command) {

        var game = loadAndValidateGame(command);
        var player = loadAndValidatePlayer(command);

        return saveDailyEntryPort.save(new DailyEntry(null, game, command.date(), player, command.points()));
    }


    @Override
    public AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var referenceValidationResult = referenceValidator.validate(addDailyEntryCommands);
        var contextualValidationResult = contextualValidator.validate(referenceValidationResult);
        var result = resolver.validateDuplicates(contextualValidationResult);
        var saved = saveDailyEntryPort.saveAll(getValidatedEntries(result));

        return new AddDailyEntriesResult(toSuccessList(saved, result), result.failureList());
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

    private List<DailyEntry> getValidatedEntries(AddDailyEntriesResult contextualValidationResult) {
        return contextualValidationResult.successList().stream().map(AddDailyEntrySuccess::entry).toList();
    }

    private List<AddDailyEntrySuccess> toSuccessList(List<DailyEntry> saved, AddDailyEntriesResult contextualValidationResult) {
        return IntStream.range(0, saved.size())
                .mapToObj(i -> new AddDailyEntrySuccess(saved.get(i), contextualValidationResult.successList().get(i).successCode()))
                .toList();
    }
}
