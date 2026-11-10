package com.pretz.geographic.application.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Objects;

public record DailyEntry(DailyEntryId dailyEntryId, Game game, LocalDate date, Player player, int points,
                         Instant submittedAt) {

    public DailyEntry {
        Objects.requireNonNull(game, "DailyEntry game must not be null");
        Objects.requireNonNull(date, "DailyEntry date must not be null");
        Objects.requireNonNull(player, "DailyEntry player must not be null");
        Objects.requireNonNull(submittedAt, "DailyEntry timestamp must not be null");
    }

    public Week getWeek() {
        return Week.of(date.get(WeekFields.ISO.weekBasedYear()),
                date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    public DailyEntryKey dailyEntryKey() {
        return new DailyEntryKey(game, player, date);
    }
}
