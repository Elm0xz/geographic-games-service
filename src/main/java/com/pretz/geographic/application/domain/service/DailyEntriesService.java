package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.AddDailyEntryCommand;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.util.List;

//TODO [GEOG-8] refactor/cleanup + tests

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

        var game = loadGamePort.loadGame(command.gameId());
        gameNameValidator.validate(command.gameName(), game.name());
        Player player;
        if (command.playerId() != null && command.playerId() > 0) {
            player = loadPlayerPort.loadPlayer(command.playerId());
            playerNameValidator.validate(command.playerName(), player.name());
        } else {
            player = loadPlayerPort.loadPlayer(command.playerName());
        }

        return saveDailyEntryPort.save(new DailyEntry(null, game, command.date(), player, command.points()));
    }

    @Override
    public List<DailyEntry> addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {
        return List.of();
    }
}
