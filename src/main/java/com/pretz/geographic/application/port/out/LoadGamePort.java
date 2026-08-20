package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;

import java.util.List;

public interface LoadGamePort {

    Game loadGame(Long id);

    List<Game> loadActiveGames();
}
