package com.pretz.geographic.application.domain.validation;

import com.pretz.geographic.application.domain.model.GameId;

public class GameNotFoundException extends RuntimeException {

    private static final String GAME_NOT_FOUND_MESSAGE = "Game with id %s doesn't exist";

    public GameNotFoundException(GameId gameId) {
        super(String.format(GAME_NOT_FOUND_MESSAGE, gameId.id()));
    }
}
