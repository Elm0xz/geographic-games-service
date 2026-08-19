package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class BaseDailyRankingCalculator implements DailyRankingCalculator {

    private static final Comparator<DailyEntry> BY_POINTS_DESC = Comparator.comparingInt(DailyEntry::points).reversed();

    @Override
    public DailyRanking calculateDailyRanking(List<DailyEntry> entries, Game game, LocalDate requestedDate) {

        return new DailyRanking(game, requestedDate, entries.stream()
                .filter(ent -> requestedDate.equals(ent.date()))
                .filter(ent -> game.equals(ent.game()))
                .sorted(BY_POINTS_DESC)
                .toList());
    }
}
