package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class BaseDailyRankingCalculatorTest {

    @Test
    public void shouldReturnDailyResult() {

        //given
        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Game game2 = new Game("Game2", ScoringSystem.STANDARD);
        LocalDate entryDate = LocalDate.of(2026, 6, 30);

        var dailyResultCalculator = new BaseDailyRankingCalculator();

        LocalDate requestedDate = LocalDate.of(2026, 6, 30);

        //when
        var result = dailyResultCalculator.calculateDailyRanking(List.of(
                        new DailyEntry(game1, entryDate, new Player("Player1"), 900),
                        new DailyEntry(game1, entryDate, new Player("Player2"), 820),
                        new DailyEntry(game1, entryDate, new Player("Player3"), 960),
                        new DailyEntry(game2, entryDate, new Player("Player1"), 430),
                        new DailyEntry(game2, entryDate, new Player("Player2"), 525),
                        new DailyEntry(game2, entryDate, new Player("Player3"), 990)),
                requestedDate, game1);

        //then
        Assertions.assertThat(result).isEqualTo(
                new DailyRanking(game1, requestedDate, List.of(
                        new DailyEntry(game1, requestedDate, new Player("Player3"), 960),
                        new DailyEntry(game1, requestedDate, new Player("Player1"), 900),
                        new DailyEntry(game1, requestedDate, new Player("Player2"), 820))
                ));
    }

    @Test
    public void shouldNotTakeIntoAccountEntriesFromDifferentDaysAndGames() {

        //given
        Game game = new Game("Game1", ScoringSystem.STANDARD);
        Game otherGame = new Game("Game2", ScoringSystem.STANDARD);
        LocalDate entryDateCorrect = LocalDate.of(2026, 6, 30);
        LocalDate entryDateIncorrect = entryDateCorrect.minusDays(1);

        //when
        var dailyResultCalculator = new BaseDailyRankingCalculator();

        LocalDate requestedDate = LocalDate.of(2026, 6, 30);

        var result = dailyResultCalculator.calculateDailyRanking(List.of(
                        new DailyEntry(game, entryDateCorrect, new Player("Player1"), 900),
                        new DailyEntry(game, entryDateCorrect, new Player("Player2"), 820),
                        new DailyEntry(game, entryDateCorrect, new Player("Player3"), 960),
                        new DailyEntry(otherGame, entryDateCorrect, new Player("Player1"), 430),
                        new DailyEntry(otherGame, entryDateCorrect, new Player("Player2"), 525),
                        new DailyEntry(otherGame, entryDateCorrect, new Player("Player3"), 990),
                        new DailyEntry(game, entryDateIncorrect, new Player("Player1"), 920),
                        new DailyEntry(game, entryDateIncorrect, new Player("Player2"), 910),
                        new DailyEntry(game, entryDateIncorrect, new Player("Player3"), 940),
                        new DailyEntry(otherGame, entryDateIncorrect, new Player("Player1"), 770),
                        new DailyEntry(otherGame, entryDateIncorrect, new Player("Player2"), 725),
                        new DailyEntry(otherGame, entryDateIncorrect, new Player("Player3"), 765)),
                requestedDate, game);

        Assertions.assertThat(result).isEqualTo(
                new DailyRanking(game, requestedDate, List.of(
                        new DailyEntry(game, requestedDate, new Player("Player3"), 960),
                        new DailyEntry(game, requestedDate, new Player("Player1"), 900),
                        new DailyEntry(game, requestedDate, new Player("Player2"), 820)))
        );
    }
}
