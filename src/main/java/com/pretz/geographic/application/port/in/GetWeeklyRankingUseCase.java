package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface GetWeeklyRankingUseCase {

    List<WeeklyRanking> getWeeklyRankings(Week week);

    //TODO [GEOG-17] implement
    WeeklyRanking getWeeklyRanking(Week week);
}
