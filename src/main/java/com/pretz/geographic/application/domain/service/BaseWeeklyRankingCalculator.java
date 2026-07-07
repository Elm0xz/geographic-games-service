package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BaseWeeklyRankingCalculator implements WeeklyRankingCalculator {

    private static final int BEST_DAYS_TO_COUNT = 5;

    /**
     * Returns weekly positions ordered from winner to lowest-ranked player.
     * <p>
     * The winner is the player with the most daily wins during the week.
     * The tie-breaker is the sum of points from the week, excluding the two weakest days.
     */
    @Override
    public WeeklyRanking calculateWeeklyRanking(List<DailyRanking> dailyRankings, Game game, Week week) {

        var filteredDailyRankings = dailyRankings.stream()
                .filter(dRank -> week.equals(dRank.getWeek()))
                .filter(dRank -> game.equals(dRank.game()))
                .toList();

        var winsByPlayer = filteredDailyRankings.stream()
                .flatMap(dRank -> dRank.getWinner().stream())
                .collect(Collectors.groupingBy(
                        player -> player,
                        Collectors.summingInt(_ -> 1)
                ));

        var entriesByPlayer = filteredDailyRankings.stream()
                .flatMap(dRank -> dRank.entries().stream())
                .collect(Collectors.groupingBy(DailyEntry::player));

        var weeklyPositions = entriesByPlayer.entrySet().stream()
                .map(entry -> new WeeklyPosition(game, week,
                        entry.getKey(),
                        winsByPlayer.getOrDefault(entry.getKey(), 0),
                        calculatePoints(entry.getValue())))
                .toList();

        return new WeeklyRanking(game, week, weeklyPositions);
    }

    private Integer calculatePoints(List<DailyEntry> playerEntries) {

        return playerEntries.stream()
                .mapToInt(DailyEntry::points)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .limit(BEST_DAYS_TO_COUNT)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
