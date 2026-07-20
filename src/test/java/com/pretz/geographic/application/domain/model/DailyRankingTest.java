package com.pretz.geographic.application.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

class DailyRankingTest {

    public static final String RANKING_ENTRY_VALIDATION_ERROR = "DailyRanking entries must match ranking game and date";

    @Test
    public void shouldReturnWinnerWhenOneExists() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(new DailyEntryId(1L), game, date, new Player(new PlayerId(1L), "Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(new DailyEntryId(2L), game, date, new Player(new PlayerId(2L), "Player2"), 970);
        DailyEntry player3Entry = new DailyEntry(new DailyEntryId(3L), game, date, new Player(new PlayerId(3L), "Player3"), 900);

        var dailyRanking = new DailyRanking(game, date, List.of(player1Entry, player2Entry, player3Entry));

        //when
        List<Player> result = dailyRanking.getWinner();

        //then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst()).isEqualTo(player1Entry.player());
    }

    @Test
    public void shouldReturnAllPlayedTiedForFirstAsWinners() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(new DailyEntryId(1L), game, date, new Player(new PlayerId(1L), "Mark"), 990);
        DailyEntry player2Entry = new DailyEntry(new DailyEntryId(2L), game, date, new Player(new PlayerId(2L), "Anne"), 990);
        DailyEntry player3Entry = new DailyEntry(new DailyEntryId(3L), game, date, new Player(new PlayerId(3L), "Andrew"), 990);
        DailyEntry player4Entry = new DailyEntry(new DailyEntryId(4L), game, date, new Player(new PlayerId(4L), "Zheng"), 900);

        var dailyRanking = new DailyRanking(game, date, List.of(player1Entry, player2Entry, player4Entry, player3Entry));

        //when
        List<Player> result = dailyRanking.getWinner();

        //then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).hasSize(3);
        Assertions.assertThat(result).isEqualTo(List.of(player1Entry.player(), player2Entry.player(), player3Entry.player()));
    }

    @Test
    public void shouldReturnEmptyListOfWinnersIfNoEntries() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        var dailyRanking = new DailyRanking(game, date, List.of());

        //when
        List<Player> result = dailyRanking.getWinner();

        //then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenEntryHasDifferentGameThanRanking() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        Game otherGame = new Game(new GameId(2L), "game2", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(new DailyEntryId(1L), game, date, new Player(new PlayerId(1L), "Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(new DailyEntryId(2L), otherGame, date, new Player(new PlayerId(2L), "Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenEntryHasDifferentDateThanRanking() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDate otherDate = date.minusDays(1);

        DailyEntry player1Entry = new DailyEntry(new DailyEntryId(1L), game, date, new Player(new PlayerId(1L), "Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(new DailyEntryId(2L), game, otherDate, new Player(new PlayerId(2L), "Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenEntryHasDifferentGameAndDateThanRanking() {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);
        Game otherGame = new Game(new GameId(2L), "game2", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDate otherDate = date.minusDays(1);

        DailyEntry player1Entry = new DailyEntry(new DailyEntryId(1L), game, date, new Player(new PlayerId(1L), "Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(new DailyEntryId(2L), otherGame, otherDate, new Player(new PlayerId(2L), "Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }

    @ParameterizedTest
    @MethodSource("dateAndWeek")
    public void shouldReturnIsoWeekForDateAtCalendarYearBoundary(LocalDate inputDate, Week expectedWeek) {

        //given
        Game game = new Game(new GameId(1L), "game1", ScoringSystem.STANDARD);

        var dailyRanking = new DailyRanking(game, inputDate, List.of());

        //when
        Week result = dailyRanking.getWeek();

        //then
        Assertions.assertThat(result).isEqualTo(expectedWeek);
    }

    public static Stream<Arguments> dateAndWeek() {
        return Stream.of(Arguments.of(LocalDate.of(2029, 12, 31), new Week(2030, 1)),
                Arguments.of(LocalDate.of(2027, 1, 1), new Week(2026, 53))
        );
    }
}