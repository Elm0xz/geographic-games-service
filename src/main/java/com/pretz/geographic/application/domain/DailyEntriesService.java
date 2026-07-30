package com.pretz.geographic.application.domain;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.AddDailyEntryCommand;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.util.List;

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
    public List<DailyEntry> addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {
        return List.of();
    }

    private Game loadAndValidateGame(AddDailyEntryCommand command) {
        var game = loadGamePort.loadGame(command.game().id());
        gameNameValidator.validate(command.game().name(), game.name());
        return game;
    }

    private Player loadAndValidatePlayer(AddDailyEntryCommand command) {
        Player player;
        if (command.player().hasId()) {
            player = loadPlayerPort.loadPlayer(command.player().id());
            playerNameValidator.validate(command.player().name(), player.name());
        } else {
            player = loadPlayerPort.loadPlayer(command.player().name());
        }
        return player;
    }
}
