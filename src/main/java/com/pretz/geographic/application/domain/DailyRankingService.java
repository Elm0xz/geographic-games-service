package com.pretz.geographic.application.domain;

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
            Comparator.comparing(it -> it.game().name());

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

    //TODO implement later
    @Override
    public DailyRanking getDailyRanking(LocalDate date, Game game) {
        return null;
    }
}
