package com.pretz.geographic.application.domain.model;

import java.util.Objects;

public record WeeklyPosition(Game game, Week week, Player player, int wins, float points) {

    public WeeklyPosition {
        Objects.requireNonNull(game, "WeeklyPosition game must not be null");
        Objects.requireNonNull(week, "WeeklyPosition week must not be null");
        Objects.requireNonNull(player, "WeeklyPosition player must not be null");
    }
}
