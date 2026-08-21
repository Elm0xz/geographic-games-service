package com.pretz.geographic.application.domain;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.service.DailyRankingCalculator;
import com.pretz.geographic.application.domain.validation.InvalidDateException;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DailyRankingService implements GetDailyRankingUseCase {

    private final LoadGamePort loadGamePort;
    private final LoadDailyEntriesPort loadDailyEntriesPort;
    private final DailyRankingCalculator dailyRankingCalculator;

    public DailyRankingService(LoadGamePort loadGamePort, LoadDailyEntriesPort loadDailyEntriesPort,
                               DailyRankingCalculator dailyRankingCalculator) {
        this.loadGamePort = loadGamePort;
        this.loadDailyEntriesPort = loadDailyEntriesPort;
        this.dailyRankingCalculator = dailyRankingCalculator;
    }

    @Override
    public List<DailyRanking> getDailyRankings(LocalDate date) {

        validatePastDate(date);

        return loadDailyEntriesPort.loadEntries(loadGamePort.loadActiveGames(), date).stream()
                .collect(Collectors.groupingBy(DailyEntry::game))
                .entrySet().stream().map(it ->
                        dailyRankingCalculator.calculateDailyRanking(it.getValue(), it.getKey(), date))
                .toList();
    }

    private static void validatePastDate(LocalDate date) {
        if (!date.isBefore(LocalDate.now())) {
            throw new InvalidDateException(String.format("Ranking calculation is possible only for past dates, input date: %s", date));
        }
    }

    //TODO implement later
    @Override
    public DailyRanking getDailyRanking(LocalDate date, Game game) {
        return null;
    }
}
