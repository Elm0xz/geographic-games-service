package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
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

    private final DailyEntryReferenceValidator referenceValidator;
    private final DailyEntryContextualValidator contextualValidator;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort,
                               LoadGamePort loadGamePort,
                               LoadPlayerPort loadPlayerPort,
                               GameNameValidator gameNameValidator,
                               PlayerNameValidator playerNameValidator,
                               DailyEntryReferenceValidator referenceValidator,
                               DailyEntryContextualValidator contextualValidator) {
        this.saveDailyEntryPort = saveDailyEntryPort;
        this.loadGamePort = loadGamePort;
        this.loadPlayerPort = loadPlayerPort;
        this.gameNameValidator = gameNameValidator;
        this.playerNameValidator = playerNameValidator;
        this.referenceValidator = referenceValidator;
        this.contextualValidator = contextualValidator;
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

        var saved = saveDailyEntryPort.saveAll(contextualValidationResult.successList().stream().map(AddDailyEntrySuccess::entry).toList());
        var successList = IntStream.range(0, saved.size())
                .mapToObj(i -> new AddDailyEntrySuccess(saved.get(i), contextualValidationResult.successList().get(i).successCode()))
                .toList();
        return new AddDailyEntriesResult(successList, contextualValidationResult.failureList());
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