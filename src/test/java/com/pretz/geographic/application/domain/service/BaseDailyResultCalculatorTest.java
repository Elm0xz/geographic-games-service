package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyResult;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class BaseDailyResultCalculatorTest {

    @Test
    public void shouldReturnDailyResult() {

        Game game1 = new Game("Game1", ScoringSystem.STANDARD);
        Game game2 = new Game("Game2", ScoringSystem.STANDARD);
        LocalDate date1 = LocalDate.now();

        var dailyResultCalculator = new BaseDailyResultCalculator();

        var result = dailyResultCalculator.calculateResults(List.of(
                new DailyEntry(game1, date1, "Player1", 900),
                new DailyEntry(game1, date1, "Player2", 820),
                new DailyEntry(game1, date1, "Player3", 960),
                new DailyEntry(game2, date1, "Player1", 430),
                new DailyEntry(game2, date1, "Player2", 525),
                new DailyEntry(game2, date1, "Player3", 990))
        );

        Assertions.assertThat(result).isEqualTo(List.of(
                new DailyResult(game1, List.of(
                        new DailyEntry(game1, date1, "Player3", 960),
                        new DailyEntry(game1, date1, "Player1", 900),
                        new DailyEntry(game1, date1, "Player2", 820))),
                new DailyResult(game2, List.of(
                        new DailyEntry(game2, date1, "Player3", 990),
                        new DailyEntry(game2, date1, "Player2", 525),
                        new DailyEntry(game2, date1, "Player1", 430)))
        ));
    }

    //TODO 2.
    @Test
    public void shouldNotTakeIntoAccountEntriesFromDifferentDays() {

    }

    //TODO 3.
    @Test
    public void shouldCallEntriesProviderPort() {

    }

}