package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public record DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points) {

    public DailyEntry {
        Objects.requireNonNull(game, "DailyEntry game must not be null");
        Objects.requireNonNull(date, "DailyEntry date must not be null");
        Objects.requireNonNull(player, "DailyEntry player must not be null");
    }
}
