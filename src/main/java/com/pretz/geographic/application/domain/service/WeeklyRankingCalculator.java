package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface WeeklyRankingCalculator {

    //TODO add game parameter and orchestrate games in service (later task)
    WeeklyRanking calculateWeeklyRanking(List<DailyRanking> dailyRankings, Integer week, Game game);
}
