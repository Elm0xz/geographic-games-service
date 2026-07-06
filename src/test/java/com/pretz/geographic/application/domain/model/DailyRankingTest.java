package com.pretz.geographic.application.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class DailyRankingTest {

    public static final String RANKING_ENTRY_VALIDATION_ERROR = "DailyRanking entries must match ranking game and date";

    @Test
    public void shouldReturnWinnerWhenOneExists() {

        //given
        Game game = new Game("game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(game, date, new Player("Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(game, date, new Player("Player2"), 970);
        DailyEntry player3Entry = new DailyEntry(game, date, new Player("Player3"), 900);

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
        Game game = new Game("game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(game, date, new Player("Mark"), 990);
        DailyEntry player2Entry = new DailyEntry(game, date, new Player("Anne"), 990);
        DailyEntry player5Entry = new DailyEntry(game, date, new Player("Andrew"), 990);
        DailyEntry player4Entry = new DailyEntry(game, date, new Player("Zheng"), 900);

        var dailyRanking = new DailyRanking(game, date, List.of(player1Entry, player2Entry, player4Entry, player5Entry));

        //when
        List<Player> result = dailyRanking.getWinner();

        //then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).hasSize(3);
        Assertions.assertThat(result).isEqualTo(List.of(player1Entry.player(), player2Entry.player(), player5Entry.player()));
    }

    @Test
    public void shouldReturnEmptyListOfWinnersIfNoEntries() {

        //given
        Game game = new Game("game1", ScoringSystem.STANDARD);
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
        Game game = new Game("game1", ScoringSystem.STANDARD);
        Game otherGame = new Game("game2", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);

        DailyEntry player1Entry = new DailyEntry(game, date, new Player("Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(otherGame, date, new Player("Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenEntryHasDifferentDateThanRanking() {

        //given
        Game game = new Game("game1", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDate otherDate = date.minusDays(1);

        DailyEntry player1Entry = new DailyEntry(game, date, new Player("Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(game, otherDate, new Player("Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenEntryHasDifferentGameAndDateThanRanking() {

        //given
        Game game = new Game("game1", ScoringSystem.STANDARD);
        Game otherGame = new Game("game2", ScoringSystem.STANDARD);
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDate otherDate = date.minusDays(1);

        DailyEntry player1Entry = new DailyEntry(game, date, new Player("Player1"), 990);
        DailyEntry player2Entry = new DailyEntry(otherGame, otherDate, new Player("Player2"), 970);

        //when //then
        Assertions.assertThatThrownBy(() -> new DailyRanking(game, date, List.of(player1Entry, player2Entry)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(RANKING_ENTRY_VALIDATION_ERROR);
    }
}
