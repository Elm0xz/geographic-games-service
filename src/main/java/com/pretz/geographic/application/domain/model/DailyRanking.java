package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DailyRanking {

    private static final Comparator<DailyEntry> BY_POINTS_DESC_THEN_PLAYER_NAME =
            Comparator.comparingInt(DailyEntry::points).reversed()
                    .thenComparing(it -> it.player().name());

    private final Game game;
    private final LocalDate date;
    private final List<DailyEntry> entries;

    private DailyRanking(Game game, LocalDate date, List<DailyEntry> entries) {
        Objects.requireNonNull(game, "DailyRanking game must not be null");
        Objects.requireNonNull(date, "DailyRanking date must not be null");
        Objects.requireNonNull(entries, "DailyRanking entries must not be null");
        validateIdsPresent(game, entries);
        validateGameAndDateMatch(game, date, entries);
        this.game = game;
        this.date = date;
        this.entries = entries;
    }

    public static DailyRanking of(Game game, LocalDate date, List<DailyEntry> entries) {
        return new DailyRanking(game, date, List.copyOf(entries.stream().sorted(BY_POINTS_DESC_THEN_PLAYER_NAME).toList()));
    }

    private static void validateIdsPresent(Game game, List<DailyEntry> entries) {
        Objects.requireNonNull(game.gameId(), "Game id must not be null");
        Objects.requireNonNull(game.gameId().id(), "Game id must not be null");
        entries.forEach(it -> Objects.requireNonNull(it.dailyEntryId(), "Daily entry id must not be null"));
        entries.forEach(it -> Objects.requireNonNull(it.dailyEntryId().id(), "Daily entry id must not be null"));
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

    public Game game() {
        return game;
    }

    public LocalDate date() {
        return date;
    }

    public List<DailyEntry> entries() {
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DailyRanking) obj;
        return Objects.equals(this.game, that.game) &&
                Objects.equals(this.date, that.date) &&
                Objects.equals(this.entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, date, entries);
    }

    @Override
    public String toString() {
        return "DailyRanking[" +
                "game=" + game + ", " +
                "date=" + date + ", " +
                "entries=" + entries + ']';
    }
}
