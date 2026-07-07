package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.List;

public interface DailyRankingCalculator {

    DailyRanking calculateDailyRanking(List<DailyEntry> entries, Game game, LocalDate requestedDate);
}
