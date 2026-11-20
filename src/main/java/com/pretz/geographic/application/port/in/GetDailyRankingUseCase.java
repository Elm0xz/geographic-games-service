package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;

import java.time.LocalDate;
import java.util.List;

public interface GetDailyRankingUseCase {

    List<DailyRanking> getDailyRankings(LocalDate date);

    List<DailyRanking> getDailyRankings(LocalDate from, LocalDate to, List<Game> games);

    List<DailyRanking> getDailyRankings(LocalDate from, LocalDate to);

    List<DailyRanking> getDailyRankings(LocalDate from, LocalDate to, GameId gameId);
}
