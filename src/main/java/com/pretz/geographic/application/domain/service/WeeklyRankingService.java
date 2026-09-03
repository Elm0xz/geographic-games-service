package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.application.domain.validation.WeekValidator;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveWeeklyRankingPort;

import java.util.List;

public class WeeklyRankingService implements GetWeeklyRankingUseCase {

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

    //TODO [GEOG-10] unit test
    @Override
    public List<WeeklyRanking> getWeeklyRankings(Week week) {

        weekValidator.validate(week);

        //only active games taken into account; game deactivation process might need some intermediate state probably?
        List<Game> activeGames = loadGamePort.loadActiveGames();

        //TODO [GEOG-10] refactor
        List<WeeklyRanking> ranking = loadWeeklyRankingPort.loadWeeklyRankings(activeGames, week);
        if (!ranking.isEmpty()) {
            return ranking;
        } else {
            List<DailyRanking> rankings = getDailyRankingUseCase.getDailyRankings(week.monday(), week.sunday());
            List<WeeklyRanking> calculatedRankings = activeGames.stream().map(it -> calculateRankingForGame(week, it, rankings)).toList();
            return saveWeeklyRankingPort.save(calculatedRankings);
        }
    }

    private WeeklyRanking calculateRankingForGame(Week week, Game game, List<DailyRanking> rankings) {
        List<DailyRanking> relevantRankings = rankings.stream().filter(it -> game.equals(it.game())).toList();
        return weeklyRankingCalculator.calculateWeeklyRanking(relevantRankings, game, week);
    }

    //TODO implement later
    @Override
    public WeeklyRanking getWeeklyRanking(Week week) {
        return null;
    }
}
