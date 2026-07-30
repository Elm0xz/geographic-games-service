package com.pretz.geographic.application.port.in;

import java.time.LocalDate;
import java.util.Objects;

public record AddDailyEntryCommand(
        GameRef game,
        PlayerRef player,
        LocalDate date,
        int points
) {

    public AddDailyEntryCommand {
        Objects.requireNonNull(game, "Game reference must not be null");
        Objects.requireNonNull(player, "Player reference must not be null");
        Objects.requireNonNull(date, "Date must not be null");

        //might need a slight refactor if we expect different time zones on the servers
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }

        if (points < 0) {
            throw new IllegalArgumentException("Points must not be negative");
        }
    }

    public record GameRef(Long id, String name) {

        public GameRef {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Game id must be positive");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Game name must not be blank");
            }
        }
    }

    public record PlayerRef(Long id, String name) {

        public PlayerRef {
            if (id != null && id <= 0) {
                throw new IllegalArgumentException("Player id must be positive when provided");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Player name must not be blank");
            }
        }

        public boolean hasId() {
            return id != null;
        }
    }
}
