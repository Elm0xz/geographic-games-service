package com.pretz.geographic.application.domain.model;

public record WeeklyPosition(Game game, Week week, Player player, Integer wins, Integer points) {
}
