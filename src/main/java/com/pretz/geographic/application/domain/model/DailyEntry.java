package com.pretz.geographic.application.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

public record DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points, Instant submittedAt) {

    public DailyEntry {
        Objects.requireNonNull(game, "DailyEntry game must not be null");
        Objects.requireNonNull(date, "DailyEntry date must not be null");
        Objects.requireNonNull(player, "DailyEntry player must not be null");
        Objects.requireNonNull(submittedAt, "DailyEntry timestamp must not be null");
    }

    //TODO get rid of this constructor -> move to test cases
    public DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points) {
        this(dailyEntryId,game, date, player, points, date.atStartOfDay().toInstant(UTC));

    }

    public Week getWeek() {
        return Week.of(date.get(WeekFields.ISO.weekBasedYear()),
                date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }
}
