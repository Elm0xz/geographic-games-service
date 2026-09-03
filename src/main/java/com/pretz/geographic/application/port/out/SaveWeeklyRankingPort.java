package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public interface SaveWeeklyRankingPort {

    List<WeeklyRanking> save(List<WeeklyRanking> rankings);
}
