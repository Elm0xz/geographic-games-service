package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Game;

import java.util.List;

public interface GetGamesUseCase {

    List<Game> getAllGames();
}
