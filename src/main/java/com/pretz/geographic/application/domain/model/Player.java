package com.pretz.geographic.application.domain.model;

import java.util.Objects;

public record Player(String name) {

    public Player {
        Objects.requireNonNull(name, "Player name must not be null");
    }
}
