package com.pretz.geographic.application.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WeeklyRanking {

    private static final Comparator<WeeklyPosition> BY_WINS_AND_POINTS_DESC =
            Comparator.comparingInt(WeeklyPosition::wins)
                    .thenComparing(WeeklyPosition::points)
                    .reversed();
    private final Game game;
    private final Week week;
    private final List<WeeklyPosition> positions;


    private WeeklyRanking(Game game, Week week, List<WeeklyPosition> positions) {
        Objects.requireNonNull(game, "WeeklyRanking game must not be null");
        Objects.requireNonNull(week, "WeeklyRanking week must not be null");
        Objects.requireNonNull(positions, "WeeklyRanking positions must not be null");

        this.game = game;
        this.week = week;
        this.positions = positions;
    }

    public static WeeklyRanking of(Game game, Week week, List<WeeklyPosition> positions) {
        validateEntriesMatch(game, week, positions);

        return new WeeklyRanking(game, week, List.copyOf(positions.stream().sorted(BY_WINS_AND_POINTS_DESC).toList()));
    }

    private static void validateEntriesMatch(Game game, Week week, List<WeeklyPosition> positions) {
        if (positions.stream().anyMatch(pos -> !game.equals(pos.game()) || !week.equals(pos.week()))) {
            throw new IllegalArgumentException("WeeklyRanking entries must match ranking game and week");
        }
    }

    public Game game() {
        return game;
    }

    public Week week() {
        return week;
    }

    public List<WeeklyPosition> positions() {
        return positions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WeeklyRanking) obj;
        return Objects.equals(this.game, that.game) &&
                Objects.equals(this.week, that.week) &&
                Objects.equals(this.positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, week, positions);
    }

    @Override
    public String toString() {
        return "WeeklyRanking[" +
                "game=" + game + ", " +
                "week=" + week + ", " +
                "positions=" + positions + ']';
    }

}
