package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyResult;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BaseDailyResultCalculator implements DailyResultCalculator {

    @Override
    public List<DailyResult> calculateResults(List<DailyEntry> entries, LocalDate requestedDate) {

        return entries.stream()
                .filter(ent -> requestedDate.equals(ent.date()))
                .collect(Collectors.groupingBy(DailyEntry::game))
                .values().stream()
                .sorted(Comparator.comparing(ent -> ent.getFirst().game(), Comparator.comparing(Game::name)))
                .map(ent -> new DailyResult(ent.getFirst().game(),
                        ent.stream().sorted(Comparator.comparingInt(DailyEntry::points).reversed()).toList()))
                .toList();
    }
}
