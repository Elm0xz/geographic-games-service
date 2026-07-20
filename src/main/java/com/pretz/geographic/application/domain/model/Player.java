package com.pretz.geographic.application.domain.model;

import java.util.Objects;

public record Player(PlayerId playerId, String name) {

    public Player {
        Objects.requireNonNull(name, "Player name must not be null");
    }
}
