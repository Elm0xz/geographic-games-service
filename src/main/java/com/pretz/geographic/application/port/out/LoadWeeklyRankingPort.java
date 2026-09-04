package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface LoadWeeklyRankingPort {

    List<WeeklyRanking> loadWeeklyRankings(List<Game> games, Week week);
}
