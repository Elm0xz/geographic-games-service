package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;

public record DailyEntry(Game game, LocalDate date, String name, Integer points) {
}
