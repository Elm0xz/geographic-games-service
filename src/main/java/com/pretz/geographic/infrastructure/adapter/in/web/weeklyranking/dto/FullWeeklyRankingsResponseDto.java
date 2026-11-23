package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.Collection;
import java.util.List;

public record FullWeeklyRankingsResponseDto(WeekResponseDto week,
                                            List<WeeklyRankingResponseDto> weeklyRankings) {

    public FullWeeklyRankingsResponseDto(Week week, Collection<WeeklyRanking> weeklyRankings) {
        this(
                new WeekResponseDto(week),
                weeklyRankings.stream().map(WeeklyRankingResponseDto::new).toList()
        );
    }
}
