package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.validation.GameNotFoundException;
import com.pretz.geographic.application.port.in.CreateGameCommand;
import com.pretz.geographic.application.port.in.CreateGameUseCase;
import com.pretz.geographic.application.port.in.GetGamesUseCase;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.SaveGamePort;

import java.util.List;

public class GameService implements GetGamesUseCase, CreateGameUseCase {

    private final LoadGamePort loadGamePort;
    private final SaveGamePort saveGamePort;

    public GameService(LoadGamePort loadGamePort, SaveGamePort saveGamePort) {
        this.loadGamePort = loadGamePort;
        this.saveGamePort = saveGamePort;
    }

    @Override
    public List<Game> getAllGames() {
        return loadGamePort.loadAllGames().stream()
                .sorted()
                .toList();
    }

    @Override
    public Game getGame(GameId gameId) {
        return loadGamePort.loadGames(List.of(gameId.id())).stream()
                .findFirst()
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    @Override
    public Game createGame(CreateGameCommand command) {
        return saveGamePort.save(new Game(null, command.name(), command.scoringSystem()));
    }
}
