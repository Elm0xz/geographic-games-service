package com.pretz.geographic.application.domain.model;

import java.util.Objects;

public record Game(String name, ScoringSystem scoringSystem) {

    public Game {
        Objects.requireNonNull(name, "Game name must not be null");
        Objects.requireNonNull(scoringSystem, "Game scoringSystem must not be null");
    }
}
