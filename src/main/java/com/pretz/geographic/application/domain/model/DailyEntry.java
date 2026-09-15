package com.pretz.geographic.application.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

public record DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points, Instant submittedAt) {

    public DailyEntry {
        Objects.requireNonNull(game, "DailyEntry game must not be null");
        Objects.requireNonNull(date, "DailyEntry date must not be null");
        Objects.requireNonNull(player, "DailyEntry player must not be null");
        Objects.requireNonNull(submittedAt, "DailyEntry timestamp must not be null");
    }

    public DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points) {
        this(dailyEntryId,game, date, player, points, date.atStartOfDay().toInstant(UTC));

    }
}
