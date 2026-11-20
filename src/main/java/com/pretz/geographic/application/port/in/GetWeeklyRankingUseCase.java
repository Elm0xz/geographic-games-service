package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface GetWeeklyRankingUseCase {

    List<WeeklyRanking> calculateWeeklyRankings(Week week);

    List<WeeklyRanking> getWeeklyRankings(Week from, Week to);

    List<WeeklyRanking> getWeeklyRankings(Week from, Week to, GameId gameId);
}
