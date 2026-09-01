package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;

import java.util.List;

public class WeeklyRankingService implements GetWeeklyRankingUseCase {

    @Override
    public List<WeeklyRanking> getWeeklyRankings(Week week) {

        //TODO 0. validate year current or past, validate week past
        //TODO 1. check if weekly ranking in db, if yes then fetch it
        //TODO 2. if not, calculate weekly ranking & persist it.

        return List.of();
    }

    //TODO implement later
    @Override
    public WeeklyRanking getWeeklyRanking(Week week) {
        return null;
    }
}
