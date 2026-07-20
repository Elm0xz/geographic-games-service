package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class BaseDailyRankingCalculatorTest {

    @Test
    public void shouldReturnDailyResult() {

        //given
        Game game1 = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        LocalDate entryDate = LocalDate.of(2026, 6, 30);

        var dailyResultCalculator = new BaseDailyRankingCalculator();

        LocalDate requestedDate = LocalDate.of(2026, 6, 30);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");

        DailyEntry game1Player1Entry = new DailyEntry(new DailyEntryId(1L), game1, entryDate, player1, 900);
        DailyEntry game1Player2Entry = new DailyEntry(new DailyEntryId(2L), game1, entryDate, player2, 820);
        DailyEntry game1Player3Entry = new DailyEntry(new DailyEntryId(3L), game1, entryDate, player3, 960);

        //when
        var result = dailyResultCalculator.calculateDailyRanking(List.of(
                        game1Player1Entry, game1Player2Entry, game1Player3Entry),
                game1, requestedDate);

        //then
        Assertions.assertThat(result).isEqualTo(
                new DailyRanking(game1, requestedDate, List.of(
                        game1Player3Entry,
                        game1Player1Entry,
                        game1Player2Entry))
        );
    }

    @Test
    public void shouldNotTakeIntoAccountEntriesFromDifferentDaysAndGames() {

        //given
        Game game = new Game(new GameId(1L), "Game1", ScoringSystem.STANDARD);
        Game otherGame = new Game(new GameId(2L), "Game2", ScoringSystem.STANDARD);
        LocalDate entryDateCorrect = LocalDate.of(2026, 6, 30);
        LocalDate entryDateIncorrect = entryDateCorrect.minusDays(1);

        LocalDate requestedDate = LocalDate.of(2026, 6, 30);

        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        Player player3 = new Player(new PlayerId(3L), "Player3");

        DailyEntry matchingPlayer1Entry = new DailyEntry(new DailyEntryId(1L), game, entryDateCorrect, player1, 900);
        DailyEntry matchingPlayer2Entry = new DailyEntry(new DailyEntryId(2L), game, entryDateCorrect, player2, 820);
        DailyEntry matchingPlayer3Entry = new DailyEntry(new DailyEntryId(3L), game, entryDateCorrect, player3, 960);
        DailyEntry otherGamePlayer1Entry = new DailyEntry(new DailyEntryId(4L), otherGame, entryDateCorrect, player1, 430);
        DailyEntry otherGamePlayer2Entry = new DailyEntry(new DailyEntryId(5L), otherGame, entryDateCorrect, player2, 525);
        DailyEntry otherGamePlayer3Entry = new DailyEntry(new DailyEntryId(6L), otherGame, entryDateCorrect, player3, 990);
        DailyEntry otherDatePlayer1Entry = new DailyEntry(new DailyEntryId(7L), game, entryDateIncorrect, player1, 920);
        DailyEntry otherDatePlayer2Entry = new DailyEntry(new DailyEntryId(8L), game, entryDateIncorrect, player2, 910);
        DailyEntry otherDatePlayer3Entry = new DailyEntry(new DailyEntryId(9L), game, entryDateIncorrect, player3, 940);
        DailyEntry otherGameOtherDatePlayer1Entry = new DailyEntry(new DailyEntryId(10L), otherGame, entryDateIncorrect, player1, 770);
        DailyEntry otherGameOtherDatePlayer2Entry = new DailyEntry(new DailyEntryId(11L), otherGame, entryDateIncorrect, player2, 725);
        DailyEntry otherGameOtherDatePlayer3Entry = new DailyEntry(new DailyEntryId(12L), otherGame, entryDateIncorrect, player3, 765);

        //when
        var dailyResultCalculator = new BaseDailyRankingCalculator();

        var result = dailyResultCalculator.calculateDailyRanking(List.of(
                        matchingPlayer1Entry, matchingPlayer2Entry, matchingPlayer3Entry,
                        otherGamePlayer1Entry, otherGamePlayer2Entry, otherGamePlayer3Entry,
                        otherDatePlayer1Entry, otherDatePlayer2Entry, otherDatePlayer3Entry,
                        otherGameOtherDatePlayer1Entry, otherGameOtherDatePlayer2Entry, otherGameOtherDatePlayer3Entry),
                game, requestedDate);

        //then
        Assertions.assertThat(result).isEqualTo(
                new DailyRanking(game, requestedDate, List.of(
                        matchingPlayer3Entry,
                        matchingPlayer1Entry,
                        matchingPlayer2Entry))
        );
    }
}