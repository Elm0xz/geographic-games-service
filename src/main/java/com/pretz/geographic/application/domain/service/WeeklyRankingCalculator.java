package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface WeeklyRankingCalculator {

    WeeklyRanking calculateWeeklyRanking(List<DailyRanking> dailyRankings, Game game, Week week);
}
