package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.List;

public interface GetDailyRankingUseCase {

    List<DailyRanking> getDailyRankings(LocalDate date);

    List<DailyRanking> getDailyRankings(LocalDate from, LocalDate to);

    //TODO [GEOG-17] implement
    DailyRanking getDailyRanking(LocalDate date, Game game);

}
