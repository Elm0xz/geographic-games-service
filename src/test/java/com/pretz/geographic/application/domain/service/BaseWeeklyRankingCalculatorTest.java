package com.pretz.geographic.application.domain.service;


import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

class BaseWeeklyRankingCalculatorTest {

    @Test
    public void shouldCalculateWeeklyResultTakingIntoAccountNumberOfWinsAndThenSumOfPointsWithout2WorstDays() {

        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        Player player3 = new Player("Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        int weekNumber = monday.get(WeekFields.ISO.weekOfWeekBasedYear());

        var weeklyResultCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyResultCalculator.calculateWeeklyRanking(List.of(
                        new DailyRanking(game1, monday, List.of(
                                new DailyEntry(game1, monday, player3, 960),
                                new DailyEntry(game1, monday, player1, 900),
                                new DailyEntry(game1, monday, player2, 820))),
                        new DailyRanking(game1, monday.plusDays(1), List.of(
                                new DailyEntry(game1, monday.plusDays(1), player1, 930),
                                new DailyEntry(game1, monday.plusDays(1), player3, 910),
                                new DailyEntry(game1, monday.plusDays(1), player2, 870))),
                        new DailyRanking(game1, monday.plusDays(2), List.of(
                                new DailyEntry(game1, monday.plusDays(2), player2, 940),
                                new DailyEntry(game1, monday.plusDays(2), player3, 900),
                                new DailyEntry(game1, monday.plusDays(2), player1, 880))),
                        new DailyRanking(game1, monday.plusDays(3), List.of(
                                new DailyEntry(game1, monday.plusDays(3), player1, 970),
                                new DailyEntry(game1, monday.plusDays(3), player3, 920),
                                new DailyEntry(game1, monday.plusDays(3), player2, 860))),
                        new DailyRanking(game1, monday.plusDays(4), List.of(
                                new DailyEntry(game1, monday.plusDays(4), player2, 950),
                                new DailyEntry(game1, monday.plusDays(4), player3, 930),
                                new DailyEntry(game1, monday.plusDays(4), player1, 910))),
                        new DailyRanking(game1, monday.plusDays(5), List.of(
                                new DailyEntry(game1, monday.plusDays(5), player3, 970),
                                new DailyEntry(game1, monday.plusDays(5), player1, 890),
                                new DailyEntry(game1, monday.plusDays(5), player2, 880))),
                        new DailyRanking(game1, monday.plusDays(6), List.of(
                                new DailyEntry(game1, monday.plusDays(6), player1, 940),
                                new DailyEntry(game1, monday.plusDays(6), player2, 920),
                                new DailyEntry(game1, monday.plusDays(6), player3, 900)))),
                weekNumber, game1);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, List.of(
                new WeeklyPosition(player1, 3, 4650),
                new WeeklyPosition(player3, 2, 4690),
                new WeeklyPosition(player2, 2, 4560))));
    }

    @Disabled
    @Test
    public void shouldCalculateWeeklyResultTreatingMissingEntriesAsZeroPointsAndNotGivingAnyWinsOnInactiveDays() {

        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        Player player3 = new Player("Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        int weekNumber = monday.get(WeekFields.ISO.weekOfWeekBasedYear());

        var weeklyResultCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyResultCalculator.calculateWeeklyRanking(List.of(
                        new DailyRanking(game1, monday, List.of(
                                new DailyEntry(game1, monday, player3, 960),
                                new DailyEntry(game1, monday, player1, 900))),
                        new DailyRanking(game1, monday.plusDays(1), List.of(
                                new DailyEntry(game1, monday.plusDays(1), player1, 930))),
                        new DailyRanking(game1, monday.plusDays(2), List.of(
                                new DailyEntry(game1, monday.plusDays(2), player2, 940),
                                new DailyEntry(game1, monday.plusDays(2), player1, 880))),
                        new DailyRanking(game1, monday.plusDays(3), List.of(
                                new DailyEntry(game1, monday.plusDays(3), player1, 970),
                                new DailyEntry(game1, monday.plusDays(3), player3, 920),
                                new DailyEntry(game1, monday.plusDays(3), player2, 860))),
                        new DailyRanking(game1, monday.plusDays(5), List.of(
                                new DailyEntry(game1, monday.plusDays(5), player3, 970),
                                new DailyEntry(game1, monday.plusDays(5), player1, 890))),
                        new DailyRanking(game1, monday.plusDays(6), List.of(
                                new DailyEntry(game1, monday.plusDays(6), player1, 940),
                                new DailyEntry(game1, monday.plusDays(6), player2, 920),
                                new DailyEntry(game1, monday.plusDays(6), player3, 900)))),
                weekNumber, game1);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, List.of(
                new WeeklyPosition(player1, 3, 4630),
                new WeeklyPosition(player3, 2, 3750),
                new WeeklyPosition(player2, 1, 2720))));
    }

    @Test
    public void shouldCalculateWeeklyResultNotTakingIntoAccountResultsOutsideTheWeek() {

        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        Player player3 = new Player("Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        int weekNumber = monday.get(WeekFields.ISO.weekOfWeekBasedYear());

        var weeklyResultCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyResultCalculator.calculateWeeklyRanking(List.of(
                        new DailyRanking(game1, monday.minusDays(1), List.of(
                                new DailyEntry(game1, monday.minusDays(1), player2, 1000),
                                new DailyEntry(game1, monday.minusDays(1), player1, 990),
                                new DailyEntry(game1, monday.minusDays(1), player3, 980))),
                        new DailyRanking(game1, monday, List.of(
                                new DailyEntry(game1, monday, player3, 975),
                                new DailyEntry(game1, monday, player1, 904),
                                new DailyEntry(game1, monday, player2, 831))),
                        new DailyRanking(game1, monday.plusDays(1), List.of(
                                new DailyEntry(game1, monday.plusDays(1), player1, 934),
                                new DailyEntry(game1, monday.plusDays(1), player3, 917),
                                new DailyEntry(game1, monday.plusDays(1), player2, 872))),
                        new DailyRanking(game1, monday.plusDays(2), List.of(
                                new DailyEntry(game1, monday.plusDays(2), player2, 948),
                                new DailyEntry(game1, monday.plusDays(2), player3, 906),
                                new DailyEntry(game1, monday.plusDays(2), player1, 883))),
                        new DailyRanking(game1, monday.plusDays(3), List.of(
                                new DailyEntry(game1, monday.plusDays(3), player1, 982),
                                new DailyEntry(game1, monday.plusDays(3), player3, 924),
                                new DailyEntry(game1, monday.plusDays(3), player2, 864))),
                        new DailyRanking(game1, monday.plusDays(4), List.of(
                                new DailyEntry(game1, monday.plusDays(4), player2, 953),
                                new DailyEntry(game1, monday.plusDays(4), player3, 937),
                                new DailyEntry(game1, monday.plusDays(4), player1, 912))),
                        new DailyRanking(game1, monday.plusDays(5), List.of(
                                new DailyEntry(game1, monday.plusDays(5), player3, 986),
                                new DailyEntry(game1, monday.plusDays(5), player1, 895),
                                new DailyEntry(game1, monday.plusDays(5), player2, 889))),
                        new DailyRanking(game1, monday.plusDays(6), List.of(
                                new DailyEntry(game1, monday.plusDays(6), player1, 946),
                                new DailyEntry(game1, monday.plusDays(6), player2, 929),
                                new DailyEntry(game1, monday.plusDays(6), player3, 908)))),
                weekNumber, game1);

        Assertions.assertThat(numberOfEntriesCheckedForWinner(result) == 7);
        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, List.of(
                new WeeklyPosition(player1, 3, 4678),
                new WeeklyPosition(player3, 2, 4739),
                new WeeklyPosition(player2, 2, 4591))));
    }

    @Test
    public void shouldCalculateWeeklyResultNotTakingIntoAccountResultsFromDifferentGame() {

        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Game game2 = new Game("Game2", ScoringSystem.STANDARD);
        Player player1 = new Player("Player1");
        Player player2 = new Player("Player2");
        Player player3 = new Player("Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        int weekNumber = monday.get(WeekFields.ISO.weekOfWeekBasedYear());

        var weeklyResultCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyResultCalculator.calculateWeeklyRanking(List.of(
                        new DailyRanking(game1, monday, List.of(
                                new DailyEntry(game1, monday, player3, 960),
                                new DailyEntry(game1, monday, player1, 900),
                                new DailyEntry(game1, monday, player2, 820))),
                        new DailyRanking(game1, monday.plusDays(1), List.of(
                                new DailyEntry(game1, monday.plusDays(1), player1, 930),
                                new DailyEntry(game1, monday.plusDays(1), player3, 910),
                                new DailyEntry(game1, monday.plusDays(1), player2, 870))),
                        new DailyRanking(game1, monday.plusDays(2), List.of(
                                new DailyEntry(game1, monday.plusDays(2), player2, 940),
                                new DailyEntry(game1, monday.plusDays(2), player3, 900),
                                new DailyEntry(game1, monday.plusDays(2), player1, 880))),
                        new DailyRanking(game1, monday.plusDays(3), List.of(
                                new DailyEntry(game1, monday.plusDays(3), player1, 970),
                                new DailyEntry(game1, monday.plusDays(3), player3, 920),
                                new DailyEntry(game1, monday.plusDays(3), player2, 860))),
                        new DailyRanking(game1, monday.plusDays(4), List.of(
                                new DailyEntry(game1, monday.plusDays(4), player2, 950),
                                new DailyEntry(game1, monday.plusDays(4), player3, 930),
                                new DailyEntry(game1, monday.plusDays(4), player1, 910))),
                        new DailyRanking(game1, monday.plusDays(5), List.of(
                                new DailyEntry(game1, monday.plusDays(5), player3, 970),
                                new DailyEntry(game1, monday.plusDays(5), player1, 890),
                                new DailyEntry(game1, monday.plusDays(5), player2, 880))),
                        new DailyRanking(game1, monday.plusDays(6), List.of(
                                new DailyEntry(game1, monday.plusDays(6), player1, 940),
                                new DailyEntry(game1, monday.plusDays(6), player2, 920),
                                new DailyEntry(game1, monday.plusDays(6), player3, 900))),
                        new DailyRanking(game2, monday, List.of(
                                new DailyEntry(game2, monday, player3, 960),
                                new DailyEntry(game2, monday, player1, 900),
                                new DailyEntry(game2, monday, player2, 820))),
                        new DailyRanking(game2, monday.plusDays(1), List.of(
                                new DailyEntry(game2, monday.plusDays(1), player1, 930),
                                new DailyEntry(game2, monday.plusDays(1), player3, 910),
                                new DailyEntry(game2, monday.plusDays(1), player2, 870))),
                        new DailyRanking(game2, monday.plusDays(2), List.of(
                                new DailyEntry(game2, monday.plusDays(2), player2, 940),
                                new DailyEntry(game2, monday.plusDays(2), player3, 900),
                                new DailyEntry(game2, monday.plusDays(2), player1, 880))),
                        new DailyRanking(game2, monday.plusDays(3), List.of(
                                new DailyEntry(game2, monday.plusDays(3), player1, 970),
                                new DailyEntry(game2, monday.plusDays(3), player3, 920),
                                new DailyEntry(game2, monday.plusDays(3), player2, 860))),
                        new DailyRanking(game2, monday.plusDays(4), List.of(
                                new DailyEntry(game2, monday.plusDays(4), player2, 950),
                                new DailyEntry(game2, monday.plusDays(4), player3, 930),
                                new DailyEntry(game2, monday.plusDays(4), player1, 910))),
                        new DailyRanking(game2, monday.plusDays(5), List.of(
                                new DailyEntry(game2, monday.plusDays(5), player3, 970),
                                new DailyEntry(game2, monday.plusDays(5), player1, 890),
                                new DailyEntry(game2, monday.plusDays(5), player2, 880))),
                        new DailyRanking(game2, monday.plusDays(6), List.of(
                                new DailyEntry(game2, monday.plusDays(6), player1, 940),
                                new DailyEntry(game2, monday.plusDays(6), player2, 920),
                                new DailyEntry(game2, monday.plusDays(6), player3, 900)))),
                weekNumber, game1);

        Assertions.assertThat(numberOfEntriesCheckedForWinner(result) == 7);
        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, List.of(
                new WeeklyPosition(player1, 3, 4650),
                new WeeklyPosition(player3, 2, 4690),
                new WeeklyPosition(player2, 2, 4560))));
    }

    private int numberOfEntriesCheckedForWinner(WeeklyRanking result) {
        return result.positions()
                .stream()
                .map(WeeklyPosition::wins)
                .mapToInt(it -> it).sum();
    }
}
