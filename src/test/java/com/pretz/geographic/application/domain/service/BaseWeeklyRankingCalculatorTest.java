package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

//TODO add given-when-then clauses
class BaseWeeklyRankingCalculatorTest {

    private long nextDailyEntryId;

    @BeforeEach
    void resetDailyEntryId() {
        nextDailyEntryId = 1L;
    }

    @Test
    public void shouldCalculateWeeklyRankingTakingIntoAccountNumberOfWinsAndThenSumOfPointsWithout2WorstDays() {

        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                        DailyRanking.of(game1, monday, List.of(
                                entry(game1, monday, player3, 960),
                                entry(game1, monday, player1, 900),
                                entry(game1, monday, player2, 820))),
                        DailyRanking.of(game1, monday.plusDays(1), List.of(
                                entry(game1, monday.plusDays(1), player1, 930),
                                entry(game1, monday.plusDays(1), player3, 910),
                                entry(game1, monday.plusDays(1), player2, 870))),
                        DailyRanking.of(game1, monday.plusDays(2), List.of(
                                entry(game1, monday.plusDays(2), player2, 940),
                                entry(game1, monday.plusDays(2), player3, 900),
                                entry(game1, monday.plusDays(2), player1, 880))),
                        DailyRanking.of(game1, monday.plusDays(3), List.of(
                                entry(game1, monday.plusDays(3), player1, 970),
                                entry(game1, monday.plusDays(3), player3, 920),
                                entry(game1, monday.plusDays(3), player2, 860))),
                        DailyRanking.of(game1, monday.plusDays(4), List.of(
                                entry(game1, monday.plusDays(4), player2, 950),
                                entry(game1, monday.plusDays(4), player3, 930),
                                entry(game1, monday.plusDays(4), player1, 910))),
                        DailyRanking.of(game1, monday.plusDays(5), List.of(
                                entry(game1, monday.plusDays(5), player3, 970),
                                entry(game1, monday.plusDays(5), player1, 890),
                                entry(game1, monday.plusDays(5), player2, 880))),
                        DailyRanking.of(game1, monday.plusDays(6), List.of(
                                entry(game1, monday.plusDays(6), player1, 940),
                                entry(game1, monday.plusDays(6), player2, 920),
                                entry(game1, monday.plusDays(6), player3, 900)))),
                game1, week);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, week, List.of(
                new WeeklyPosition(game1, week, player1, 3, 4650),
                new WeeklyPosition(game1, week, player3, 2, 4690),
                new WeeklyPosition(game1, week, player2, 2, 4560))));
    }

    @Test
    public void shouldCalculateWeeklyRankingTreatingMissingEntriesAsZeroPointsAndNotGivingAnyWinsOnInactiveDays() {

        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                        DailyRanking.of(game1, monday, List.of(
                                entry(game1, monday, player3, 960),
                                entry(game1, monday, player1, 900))),
                        DailyRanking.of(game1, monday.plusDays(1), List.of(
                                entry(game1, monday.plusDays(1), player1, 930))),
                        DailyRanking.of(game1, monday.plusDays(2), List.of(
                                entry(game1, monday.plusDays(2), player2, 940),
                                entry(game1, monday.plusDays(2), player1, 880))),
                        DailyRanking.of(game1, monday.plusDays(3), List.of(
                                entry(game1, monday.plusDays(3), player1, 970),
                                entry(game1, monday.plusDays(3), player3, 920),
                                entry(game1, monday.plusDays(3), player2, 860))),
                        DailyRanking.of(game1, monday.plusDays(5), List.of(
                                entry(game1, monday.plusDays(5), player3, 970),
                                entry(game1, monday.plusDays(5), player1, 890))),
                        DailyRanking.of(game1, monday.plusDays(6), List.of(
                                entry(game1, monday.plusDays(6), player1, 940),
                                entry(game1, monday.plusDays(6), player2, 920),
                                entry(game1, monday.plusDays(6), player3, 900)))),
                game1, week);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, week, List.of(
                new WeeklyPosition(game1, week, player1, 3, 4630),
                new WeeklyPosition(game1, week, player3, 2, 3750),
                new WeeklyPosition(game1, week, player2, 1, 2720))));
    }

    @Test
    public void shouldCalculateWeeklyRankingNotTakingIntoAccountDailyRankingsOutsideTheWeek() {

        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                        DailyRanking.of(game1, monday.minusDays(1), List.of(
                                entry(game1, monday.minusDays(1), player2, 1000),
                                entry(game1, monday.minusDays(1), player1, 990),
                                entry(game1, monday.minusDays(1), player3, 980))),
                        DailyRanking.of(game1, monday, List.of(
                                entry(game1, monday, player3, 975),
                                entry(game1, monday, player1, 904),
                                entry(game1, monday, player2, 831))),
                        DailyRanking.of(game1, monday.plusDays(1), List.of(
                                entry(game1, monday.plusDays(1), player1, 934),
                                entry(game1, monday.plusDays(1), player3, 917),
                                entry(game1, monday.plusDays(1), player2, 872))),
                        DailyRanking.of(game1, monday.plusDays(2), List.of(
                                entry(game1, monday.plusDays(2), player2, 948),
                                entry(game1, monday.plusDays(2), player3, 906),
                                entry(game1, monday.plusDays(2), player1, 883))),
                        DailyRanking.of(game1, monday.plusDays(3), List.of(
                                entry(game1, monday.plusDays(3), player1, 982),
                                entry(game1, monday.plusDays(3), player3, 924),
                                entry(game1, monday.plusDays(3), player2, 864))),
                        DailyRanking.of(game1, monday.plusDays(4), List.of(
                                entry(game1, monday.plusDays(4), player2, 953),
                                entry(game1, monday.plusDays(4), player3, 937),
                                entry(game1, monday.plusDays(4), player1, 912))),
                        DailyRanking.of(game1, monday.plusDays(5), List.of(
                                entry(game1, monday.plusDays(5), player3, 986),
                                entry(game1, monday.plusDays(5), player1, 895),
                                entry(game1, monday.plusDays(5), player2, 889))),
                        DailyRanking.of(game1, monday.plusDays(6), List.of(
                                entry(game1, monday.plusDays(6), player1, 946),
                                entry(game1, monday.plusDays(6), player2, 929),
                                entry(game1, monday.plusDays(6), player3, 908)))),
                game1, week);

        Assertions.assertThat(numberOfEntriesCheckedForWinner(result)).isEqualTo(7);
        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, week, List.of(
                new WeeklyPosition(game1, week, player1, 3, 4678),
                new WeeklyPosition(game1, week, player3, 2, 4739),
                new WeeklyPosition(game1, week, player2, 2, 4591))));
    }

    @Test
    public void shouldCalculateWeeklyRankingNotTakingIntoAccountDailyRankingsFromDifferentGame() {

        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Game game2 = new Game(new GameId(2L), "Game2", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                        DailyRanking.of(game1, monday, List.of(
                                entry(game1, monday, player3, 960),
                                entry(game1, monday, player1, 900),
                                entry(game1, monday, player2, 820))),
                        DailyRanking.of(game1, monday.plusDays(1), List.of(
                                entry(game1, monday.plusDays(1), player1, 930),
                                entry(game1, monday.plusDays(1), player3, 910),
                                entry(game1, monday.plusDays(1), player2, 870))),
                        DailyRanking.of(game1, monday.plusDays(2), List.of(
                                entry(game1, monday.plusDays(2), player2, 940),
                                entry(game1, monday.plusDays(2), player3, 900),
                                entry(game1, monday.plusDays(2), player1, 880))),
                        DailyRanking.of(game1, monday.plusDays(3), List.of(
                                entry(game1, monday.plusDays(3), player1, 970),
                                entry(game1, monday.plusDays(3), player3, 920),
                                entry(game1, monday.plusDays(3), player2, 860))),
                        DailyRanking.of(game1, monday.plusDays(4), List.of(
                                entry(game1, monday.plusDays(4), player2, 950),
                                entry(game1, monday.plusDays(4), player3, 930),
                                entry(game1, monday.plusDays(4), player1, 910))),
                        DailyRanking.of(game1, monday.plusDays(5), List.of(
                                entry(game1, monday.plusDays(5), player3, 970),
                                entry(game1, monday.plusDays(5), player1, 890),
                                entry(game1, monday.plusDays(5), player2, 880))),
                        DailyRanking.of(game1, monday.plusDays(6), List.of(
                                entry(game1, monday.plusDays(6), player1, 940),
                                entry(game1, monday.plusDays(6), player2, 920),
                                entry(game1, monday.plusDays(6), player3, 900))),
                        DailyRanking.of(game2, monday, List.of(
                                entry(game2, monday, player3, 960),
                                entry(game2, monday, player1, 900),
                                entry(game2, monday, player2, 820)))),
                game1, week);

        Assertions.assertThat(numberOfEntriesCheckedForWinner(result)).isEqualTo(7);
        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, week, List.of(
                new WeeklyPosition(game1, week, player1, 3, 4650),
                new WeeklyPosition(game1, week, player3, 2, 4690),
                new WeeklyPosition(game1, week, player2, 2, 4560))));
    }

    @Test
    public void shouldCalculateWeeklyRankingWithTiedDailyWinners() {

        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                        DailyRanking.of(game1, monday, List.of(
                                entry(game1, monday, player3, 960),
                                entry(game1, monday, player1, 960),
                                entry(game1, monday, player2, 960))),
                        DailyRanking.of(game1, monday.plusDays(1), List.of(
                                entry(game1, monday.plusDays(1), player1, 920),
                                entry(game1, monday.plusDays(1), player3, 920),
                                entry(game1, monday.plusDays(1), player2, 900))),
                        DailyRanking.of(game1, monday.plusDays(2), List.of(
                                entry(game1, monday.plusDays(2), player1, 910),
                                entry(game1, monday.plusDays(2), player2, 910),
                                entry(game1, monday.plusDays(2), player3, 850)))),
                game1, week);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game1, week, List.of(
                new WeeklyPosition(game1, week, player1, 3, 2790),
                new WeeklyPosition(game1, week, player2, 2, 2770),
                new WeeklyPosition(game1, week, player3, 2, 2730))));
    }

    @Test
    public void shouldReturnEmptyWeeklyRankingWhenThereAreNoDailyRankings() {
        Game game = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Week week = new Week(2026, 4);

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(), game, week);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game, week, List.of()));
    }

    @Test
    public void shouldReturnEmptyWeeklyRankingWhenMatchingDailyRankingsHaveNoEntries() {
        Game game = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Week week = new Week(2026, monday.get(WeekFields.ISO.weekOfWeekBasedYear()));

        var weeklyRankingCalculator = new BaseWeeklyRankingCalculator();

        var result = weeklyRankingCalculator.calculateWeeklyRanking(List.of(
                DailyRanking.of(game, monday, List.of()),
                DailyRanking.of(game, monday.plusDays(1), List.of())
        ), game, week);

        Assertions.assertThat(result).isEqualTo(new WeeklyRanking(game, week, List.of()));
    }

    private DailyEntry entry(Game game, LocalDate date, Player player, int points) {
        return new DailyEntry(new DailyEntryId(nextDailyEntryId++), game, date, player, points);
    }

    private int numberOfEntriesCheckedForWinner(WeeklyRanking ranking) {
        return ranking.positions()
                .stream()
                .map(WeeklyPosition::wins)
                .mapToInt(it -> it).sum();
    }
}
