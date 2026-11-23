package com.pretz.geographic.application.domain.validation;

import com.pretz.geographic.application.domain.model.PlayerId;

public class PlayerNotFoundException extends RuntimeException {

    private static final String PLAYER_NOT_FOUND_MESSAGE = "Player with id %s doesn't exist";

    public PlayerNotFoundException(PlayerId playerId) {
        super(String.format(PLAYER_NOT_FOUND_MESSAGE, playerId.id()));
    }
}
