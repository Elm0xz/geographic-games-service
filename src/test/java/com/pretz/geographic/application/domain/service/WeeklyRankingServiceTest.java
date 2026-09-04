package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.application.domain.validation.InvalidDateException;
import com.pretz.geographic.application.domain.validation.WeekValidator;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveWeeklyRankingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyRankingServiceTest {

    @Mock
    private LoadWeeklyRankingPort loadWeeklyRankingPort;

    @Mock
    private SaveWeeklyRankingPort saveWeeklyRankingPort;

    @Mock
    private LoadGamePort loadGamePort;

    @Mock
    private WeeklyRankingCalculator weeklyRankingCalculator;

    @Mock
    private GetDailyRankingUseCase getDailyRankingUseCase;

    private WeeklyRankingService weeklyRankingService;

    private static final Game GAME_1 = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
    private static final Game GAME_2 = new Game(new GameId(2L), "WhenTaken", ScoringSystem.STANDARD);
    private static final Player PLAYER_1 = new Player(new PlayerId(1L), "Player1");
    private static final Player PLAYER_2 = new Player(new PlayerId(2L), "Player2");

    @BeforeEach
    void setUp() {
        weeklyRankingService = new WeeklyRankingService(
                loadWeeklyRankingPort,
                saveWeeklyRankingPort,
                loadGamePort,
                new WeekValidator(),
                weeklyRankingCalculator,
                getDailyRankingUseCase
        );
    }

    @Test
    void shouldReturnAllRankingsFromDbWhenAllAlreadyCalculated() {

        //given
        Week week = pastWeek();
        WeeklyRanking ranking1 = rankingFor(GAME_1, week);
        WeeklyRanking ranking2 = rankingFor(GAME_2, week);

        when(loadGamePort.loadActiveGames()).thenReturn(List.of(GAME_1, GAME_2));
        when(loadWeeklyRankingPort.loadWeeklyRankings(List.of(GAME_1, GAME_2), week))
                .thenReturn(List.of(ranking1, ranking2));

        //when
        List<WeeklyRanking> result = weeklyRankingService.getWeeklyRankings(week);

        //then
        assertThat(result).containsExactlyInAnyOrder(ranking1, ranking2);
        verify(getDailyRankingUseCase, never()).getDailyRankings(any(), any(), anyList());
        verify(saveWeeklyRankingPort, never()).save(anyList());
    }

    @Test
    void shouldReturnMixedRankingsWhenSomeAlreadyInDbAndSomeNew() {

        //given
        Week week = pastWeek();
        WeeklyRanking existingRanking = rankingFor(GAME_1, week);
        WeeklyRanking newRanking = rankingFor(GAME_2, week);

        List<DailyRanking> dailyRankingsForGame2 = List.of(); // no daily entries needed for unit scope

        when(loadGamePort.loadActiveGames()).thenReturn(List.of(GAME_1, GAME_2));
        when(loadWeeklyRankingPort.loadWeeklyRankings(List.of(GAME_1, GAME_2), week))
                .thenReturn(List.of(existingRanking));
        when(getDailyRankingUseCase.getDailyRankings(week.monday(), week.sunday(), List.of(GAME_2)))
                .thenReturn(dailyRankingsForGame2);
        when(weeklyRankingCalculator.calculateWeeklyRanking(dailyRankingsForGame2, GAME_2, week))
                .thenReturn(newRanking);
        when(saveWeeklyRankingPort.save(List.of(newRanking))).thenReturn(List.of(newRanking));

        //when
        List<WeeklyRanking> result = weeklyRankingService.getWeeklyRankings(week);

        //then
        assertThat(result).containsExactlyInAnyOrder(existingRanking, newRanking);
        verify(saveWeeklyRankingPort).save(List.of(newRanking));
    }

    @Test
    void shouldCalculateAndSaveAllRankingsWhenNoneExistInDb() {

        //given
        Week week = pastWeek();
        WeeklyRanking newRanking1 = rankingFor(GAME_1, week);
        WeeklyRanking newRanking2 = rankingFor(GAME_2, week);

        List<DailyRanking> dailyRankingsForGame2 = List.of();

        when(loadGamePort.loadActiveGames()).thenReturn(List.of(GAME_1, GAME_2));
        when(loadWeeklyRankingPort.loadWeeklyRankings(List.of(GAME_1, GAME_2), week))
                .thenReturn(List.of());
        when(getDailyRankingUseCase.getDailyRankings(week.monday(), week.sunday(), List.of(GAME_1, GAME_2)))
                .thenReturn(dailyRankingsForGame2);
        when(weeklyRankingCalculator.calculateWeeklyRanking(List.of(), GAME_1, week)).thenReturn(newRanking1);
        when(weeklyRankingCalculator.calculateWeeklyRanking(List.of(), GAME_2, week)).thenReturn(newRanking2);
        when(saveWeeklyRankingPort.save(anyList())).thenAnswer(inv -> inv.getArgument(0));

        //when
        List<WeeklyRanking> result = weeklyRankingService.getWeeklyRankings(week);

        //then
        assertThat(result).containsExactlyInAnyOrder(newRanking1, newRanking2);
        verify(saveWeeklyRankingPort).save(List.of(newRanking1, newRanking2));
    }

    @MethodSource("invalidWeeks")
    @ParameterizedTest
    void shouldThrowInvalidDateExceptionOnInvalidWeeks(LocalDate wrongDate) {

        //given
        Week currentWeek = weekFromDate(wrongDate);

        //when, then
        assertThatThrownBy(() -> weeklyRankingService.getWeeklyRankings(currentWeek))
                .isInstanceOf(InvalidDateException.class);
    }

    private static WeeklyRanking rankingFor(Game game, Week week) {
        WeeklyPosition pos1 = new WeeklyPosition(game, week, PLAYER_1, 3, 4500);
        WeeklyPosition pos2 = new WeeklyPosition(game, week, PLAYER_2, 2, 4200);
        return new WeeklyRanking(game, week, List.of(pos1, pos2));
    }

    private static Week weekFromDate(LocalDate date) {
        int year = date.get(WeekFields.ISO.weekBasedYear());
        int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
        return new Week(year, week);
    }

    private static Week pastWeek() {
        LocalDate lastWeekMonday = LocalDate.now().minusWeeks(1).with(WeekFields.ISO.dayOfWeek(), 1);
        return weekFromDate(lastWeekMonday);
    }

    private static Stream<Arguments> invalidWeeks() {
        return Stream.of(
                Arguments.of(LocalDate.now()),
                Arguments.of(LocalDate.now().plusWeeks(3)),
                Arguments.of(LocalDate.now().plusYears(1)));
    }
}