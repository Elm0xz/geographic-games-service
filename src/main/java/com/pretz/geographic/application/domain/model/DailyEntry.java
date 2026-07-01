package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;

public record DailyEntry(Game game, LocalDate date, Player player, Integer points) {
}
