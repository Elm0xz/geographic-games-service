package com.pretz.geographic.application.domain.model;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;

public record DailyRanking(Game game, LocalDate date, List<DailyEntry> entries) {

    public static final Comparator<DailyEntry> BY_POINTS_DESC = Comparator.comparingInt(DailyEntry::points).reversed();

    public DailyRanking {
        if (entries.stream().anyMatch(entry -> !game.equals(entry.game()) || !date.equals(entry.date()))) {
            throw new IllegalArgumentException("DailyRanking entries must match ranking game and date");
        }

        entries = entries.stream()
                .sorted(BY_POINTS_DESC)
                .toList();
    }

    public Week getWeek() {
        return new Week(date.getYear(), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    public List<Player> getWinner() {

        if (entries.isEmpty()) return List.of();
        else return getAllWinners();
    }

    private List<Player> getAllWinners() {
        var winningResult = entries.getFirst().points();

        return entries.stream()
                .filter(ent -> winningResult.equals(ent.points()))
                .map(DailyEntry::player)
                .toList();
    }
}
