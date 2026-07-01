package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseWeeklyRankingCalculator implements WeeklyRankingCalculator {

    /**
     * should return list of weekly positions ordered in descending order starting from the winner
     * the winner is the person with most daily wins throughout the week
     * tiebreaker is the sum(mean?) of points won during the week, skipping two weakest days
     *
     * @param dailyRankings
     * @param week
     * @param game
     * @return
     */
    @Override
    public WeeklyRanking calculateWeeklyRanking(List<DailyRanking> dailyRankings, Integer week, Game game) {

        var wins = dailyRankings.stream()
                .filter(dailyRanking -> week.equals(dailyRanking.getWeek()))
                .filter(dailyRanking -> game.equals(dailyRanking.game()))
                .map(DailyRanking::getWinner)
                .reduce(new HashMap<>(), this::addWinForPlayer,
                        (a, b) ->
                                Stream.concat(a.entrySet().stream(), b.entrySet().stream())
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        var points = dailyRankings.stream()
                .filter(dailyRanking -> week.equals(dailyRanking.getWeek()))
                .filter(dailyRanking -> game.equals(dailyRanking.game()))
                .flatMap(it -> it.entries().stream())
                .collect(Collectors.groupingBy(DailyEntry::player));

        var pointsSum = points.entrySet().stream()
                .map(it -> new WeeklyPosition(it.getKey(), wins.get(it.getKey()), calculatePoints(it.getValue())))
                .sorted(Comparator.comparingInt(WeeklyPosition::wins).thenComparing(WeeklyPosition::points).reversed())
                .toList();

        return new WeeklyRanking(game, pointsSum);

    }

    private Map<Player, Integer> addWinForPlayer(Map<Player, Integer> weeklyWins, Player dailyWinner) {

        if (!weeklyWins.containsKey(dailyWinner)) {
            weeklyWins.put(dailyWinner, 1);
        } else weeklyWins.compute(dailyWinner, (_, wins) -> wins + 1);

        return weeklyWins;
    }

    private Integer calculatePoints(List<DailyEntry> playerEntries) {

        return playerEntries.stream()
                .sorted(Comparator.comparingInt(DailyEntry::points)).skip(2)
                .mapToInt(DailyEntry::points).sum();
    }
}
