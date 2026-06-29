package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyResult;
import com.pretz.geographic.application.domain.model.Game;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BaseDailyResultCalculator implements DailyResultCalculator {

    @Override
    public List<DailyResult> calculateResults(List<DailyEntry> entries) {

        return entries.stream()
                .collect(Collectors.groupingBy(DailyEntry::game))
                .values().stream()
                .sorted(Comparator.comparing(it -> it.getFirst().game(), Comparator.comparing(Game::name)))
                .map(it -> new DailyResult(it.getFirst().game(),
                        it.stream().sorted(Comparator.comparingInt(DailyEntry::points).reversed()).toList()))
                .toList();
    }
}
