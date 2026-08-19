package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Objects;

public record DailyRanking(Game game, LocalDate date, List<DailyEntry> entries) {

    public DailyRanking {
        Objects.requireNonNull(game, "DailyRanking game must not be null");
        Objects.requireNonNull(date, "DailyRanking date must not be null");
        Objects.requireNonNull(entries, "DailyRanking entries must not be null");
        validateGameAndDateMatch(game, date, entries);
        validateEntriesPointsDescending(entries);
    }

    private static void validateEntriesPointsDescending(List<DailyEntry> entries) {

        for (int i = 1; i < entries.size(); i++) {
            if (entries.get(i - 1).points() < entries.get(i).points()) {
                throw new IllegalArgumentException("DailyRanking entries must be sorted by points in descending order");
            }
        }
    }

    private static void validateGameAndDateMatch(Game game, LocalDate date, List<DailyEntry> entries) {

        if (entries.stream().anyMatch(entry -> !game.equals(entry.game()) || !date.equals(entry.date()))) {
            throw new IllegalArgumentException("DailyRanking entries must match ranking game and date");
        }
    }

    public Week getWeek() {
        return new Week(date.get(WeekFields.ISO.weekBasedYear()),
                date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    public List<Player> getWinner() {

        if (entries.isEmpty()) return List.of();
        else return getAllWinners();
    }

    private List<Player> getAllWinners() {
        var winningResult = entries.getFirst().points();

        return entries.stream()
                .filter(ent -> winningResult == ent.points())
                .map(DailyEntry::player)
                .toList();
    }
}
