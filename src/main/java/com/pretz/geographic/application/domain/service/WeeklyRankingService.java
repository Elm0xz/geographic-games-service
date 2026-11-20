package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.application.domain.validation.GameNotFoundException;
import com.pretz.geographic.application.domain.validation.WeekValidator;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveWeeklyRankingPort;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class WeeklyRankingService implements GetWeeklyRankingUseCase {

    private static final Comparator<WeeklyRanking> BY_WEEK_AND_GAME_NAME =
            Comparator.comparing(WeeklyRanking::week).thenComparing(WeeklyRanking::game);

    private final LoadWeeklyRankingPort loadWeeklyRankingPort;
    private final SaveWeeklyRankingPort saveWeeklyRankingPort;
    private final LoadGamePort loadGamePort;
    private final WeekValidator weekValidator;
    private final WeeklyRankingCalculator weeklyRankingCalculator;
    private final GetDailyRankingUseCase getDailyRankingUseCase;

    public WeeklyRankingService(LoadWeeklyRankingPort loadWeeklyRankingPort,
                                SaveWeeklyRankingPort saveWeeklyRankingPort,
                                LoadGamePort loadGamePort,
                                WeekValidator weekValidator,
                                WeeklyRankingCalculator weeklyRankingCalculator,
                                GetDailyRankingUseCase getDailyRankingUseCase) {
        this.loadWeeklyRankingPort = loadWeeklyRankingPort;
        this.saveWeeklyRankingPort = saveWeeklyRankingPort;
        this.loadGamePort = loadGamePort;
        this.weekValidator = weekValidator;
        this.weeklyRankingCalculator = weeklyRankingCalculator;
        this.getDailyRankingUseCase = getDailyRankingUseCase;
    }

    /**
     * Returns weekly rankings of all active games for the week, calculating and persisting the missing ones. Once
     * persisted, a ranking closes its week for new daily entries of the game.
     */
    @Override
    public List<WeeklyRanking> calculateWeeklyRankings(Week week) {

        weekValidator.validate(week);

        //TODO [GEOG-18] only active games taken into account; game deactivation process will need intermediate state to be added
        var activeGames = loadGamePort.loadActiveGames();
        var weeklyRankings = loadWeeklyRankingPort.loadWeeklyRankings(activeGames, week);
        var calculatedGames = weeklyRankings.stream().map(WeeklyRanking::game).toList();
        var gamesToCalculate = activeGames.stream().filter(it -> !calculatedGames.contains(it)).toList();
        if (gamesToCalculate.isEmpty()) return weeklyRankings;
        else {
            var dailyRankings = getDailyRankingUseCase.getDailyRankings(week.monday(), week.sunday(), gamesToCalculate);
            var newWeeklyRankings = gamesToCalculate.stream()
                    .map(game -> calculateRankingForGame(week, game, dailyRankings)).toList();
            if (isNonEmptyRankingPresent(newWeeklyRankings)) {
                saveWeeklyRankingPort.save(newWeeklyRankings.stream().filter(it -> !it.positions().isEmpty()).toList());
            }
            return Stream.concat(weeklyRankings.stream(), newWeeklyRankings.stream()).toList();
        }
    }

    @Override
    public List<WeeklyRanking> getWeeklyRankings(Week from, Week to) {

        weekValidator.validateInterval(from, to);

        return loadPersistedRankings(loadGamePort.loadAllGames(), from, to);
    }

    @Override
    public List<WeeklyRanking> getWeeklyRankings(Week from, Week to, GameId gameId) {

        weekValidator.validateInterval(from, to);

        var game = loadGamePort.loadGames(List.of(gameId.id())).stream()
                .findFirst()
                .orElseThrow(() -> new GameNotFoundException(gameId));
        return loadPersistedRankings(List.of(game), from, to);
    }

    private List<WeeklyRanking> loadPersistedRankings(List<Game> games, Week from, Week to) {
        return loadWeeklyRankingPort.loadWeeklyRankings(games, from, to).stream()
                .sorted(BY_WEEK_AND_GAME_NAME)
                .toList();
    }

    private WeeklyRanking calculateRankingForGame(Week week, Game game, List<DailyRanking> rankings) {
        List<DailyRanking> relevantRankings = rankings.stream().filter(it -> game.equals(it.game())).toList();
        return weeklyRankingCalculator.calculateWeeklyRanking(relevantRankings, game, week);
    }

    private boolean isNonEmptyRankingPresent(List<WeeklyRanking> rankings) {
        return !rankings.stream().filter(it -> !it.positions().isEmpty()).toList().isEmpty();
    }
}
