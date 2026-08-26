package com.pretz.geographic.application.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

//TODO [GEOG-10] Should be converted into java class with private constructor that enforces sorting (see `DailyRanking`)
public record WeeklyRanking(Game game, Week week, List<WeeklyPosition> positions) {

    private static final Comparator<WeeklyPosition> BY_WINS_AND_POINTS_DESC =
            Comparator.comparingInt(WeeklyPosition::wins)
                    .thenComparingInt(WeeklyPosition::points)
                    .reversed();

    public WeeklyRanking {
        Objects.requireNonNull(game, "WeeklyRanking game must not be null");
        Objects.requireNonNull(week, "WeeklyRanking week must not be null");
        Objects.requireNonNull(positions, "WeeklyRanking positions must not be null");

        if (positions.stream().anyMatch(pos -> !game.equals(pos.game()) || !week.equals(pos.week()))) {
            throw new IllegalArgumentException("WeeklyRanking entries must match ranking game and week");
        }

        positions = positions.stream().sorted(BY_WINS_AND_POINTS_DESC).toList();
    }
}
