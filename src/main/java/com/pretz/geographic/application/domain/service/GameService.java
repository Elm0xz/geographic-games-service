package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.in.GetGamesUseCase;
import com.pretz.geographic.application.port.out.LoadGamePort;

import java.util.List;

public class GameService implements GetGamesUseCase {

    private final LoadGamePort loadGamePort;

    public GameService(LoadGamePort loadGamePort) {
        this.loadGamePort = loadGamePort;
    }

    @Override
    public List<Game> getAllGames() {
        return loadGamePort.loadAllGames().stream()
                .sorted()
                .toList();
    }
}
