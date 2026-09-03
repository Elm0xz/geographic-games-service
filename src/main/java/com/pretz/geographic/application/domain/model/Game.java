package com.pretz.geographic.application.domain.model;

import java.util.Objects;

public record Game(GameId gameId, String name, ScoringSystem scoringSystem) implements Comparable<Game> {

    public Game {
        Objects.requireNonNull(name, "Game name must not be null");
        Objects.requireNonNull(scoringSystem, "Game scoringSystem must not be null");
    }

    //TODO [GEOG-10] unit test?
    @Override
    public int compareTo(Game o) {
        return this.name().compareTo(o.name());
    }
}
