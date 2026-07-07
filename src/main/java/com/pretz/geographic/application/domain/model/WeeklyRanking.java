package com.pretz.geographic.application.domain.model;

import java.util.Comparator;
import java.util.List;

//TODO Require non null validation for this and other domain objects
public record WeeklyRanking(Game game, Week week, List<WeeklyPosition> positions) {

    private static final Comparator<WeeklyPosition> BY_WINS_AND_POINTS_DESC =
            Comparator.comparingInt(WeeklyPosition::wins)
                    .thenComparingInt(WeeklyPosition::points)
                    .reversed();

    public WeeklyRanking {
        if (positions.stream().anyMatch(pos -> !game.equals(pos.game()) || !week.equals(pos.week()))) {
            throw new IllegalArgumentException("WeeklyRanking entries must match ranking game and week");
        }

        positions = positions.stream().sorted(BY_WINS_AND_POINTS_DESC).toList();
    }
}
