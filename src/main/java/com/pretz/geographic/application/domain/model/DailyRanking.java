package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

public record DailyRanking(Game game, LocalDate date, List<DailyEntry> entries) {

    public Integer getWeek() {
        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public Player getWinner() {
        return entries.getFirst().player();
    }
}
