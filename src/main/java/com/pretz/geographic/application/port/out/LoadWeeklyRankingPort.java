package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;
import java.util.Set;

public interface LoadWeeklyRankingPort {

    /**
     * Loads persisted weekly rankings of given games for all weeks between {@code from} and {@code to}, both inclusive.
     */
    List<WeeklyRanking> loadWeeklyRankings(List<Game> games, Week from, Week to);

    Set<GameWeek> loadCalculatedWeeks(List<GameWeek> gameWeeks);
}
