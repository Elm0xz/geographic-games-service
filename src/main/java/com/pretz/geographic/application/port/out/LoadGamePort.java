package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;

import java.util.List;

public interface LoadGamePort {

    List<Game> loadActiveGames();

    List<Game> loadGames(List<Long> gameIds);
}
