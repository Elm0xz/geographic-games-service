package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.validation.RankingDateValidator;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DailyRankingService implements GetDailyRankingUseCase {

    private static final Comparator<DailyRanking> BY_GAME_NAME =
            Comparator.comparing(DailyRanking::game);

    private static final Comparator<DailyRanking> BY_GAME_NAME_AND_DATE =
            Comparator.comparing(DailyRanking::game).thenComparing(DailyRanking::date);

    private final LoadGamePort loadGamePort;
    private final LoadDailyEntriesPort loadDailyEntriesPort;
    private final RankingDateValidator rankingDateValidator;

    public DailyRankingService(LoadGamePort loadGamePort,
                               LoadDailyEntriesPort loadDailyEntriesPort,
                               RankingDateValidator rankingDateValidator) {
        this.loadGamePort = loadGamePort;
        this.loadDailyEntriesPort = loadDailyEntriesPort;
        this.rankingDateValidator = rankingDateValidator;
    }

    @Override
    public List<DailyRanking> getDailyRankings(LocalDate date) {

        rankingDateValidator.validatePastDate(date);

        //ranking isn't persisted but calculated on demand; active games without results for the day won't be taken into account
        return loadDailyEntriesPort.loadEntries(loadGamePort.loadActiveGames(), date).stream()
                .collect(Collectors.groupingBy(DailyEntry::game))
                .entrySet().stream().map(it -> DailyRanking.of(it.getKey(), date, it.getValue()))
                .sorted(BY_GAME_NAME)
                .toList();
    }

    //TODO [GEOG-10] Add unit test
    @Override
    public List<DailyRanking> getDailyRankings(LocalDate from, LocalDate to, List<Game> games) {

        rankingDateValidator.validatePastDate(to);

        return loadDailyEntriesPort.loadEntries(loadGamePort.loadGames(
                        games.stream().map(it -> it.gameId().id()).toList()), from, to).stream()
                .collect(Collectors.groupingBy(it -> new GameAndDate(it.game(), it.date())))
                .entrySet().stream().map(it -> DailyRanking.of(it.getKey().game(), it.getKey().date(), it.getValue()))
                .sorted(BY_GAME_NAME_AND_DATE)
                .toList();
    }

    //TODO [GEOG-17] implement later
    @Override
    public DailyRanking getDailyRanking(LocalDate date, Game game) {
        return null;
    }

    record GameAndDate(Game game, LocalDate date) {
    }
}
