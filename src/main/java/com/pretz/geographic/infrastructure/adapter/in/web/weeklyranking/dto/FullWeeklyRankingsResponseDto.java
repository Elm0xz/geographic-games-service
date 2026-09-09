package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.time.Year;
import java.util.List;

public record FullWeeklyRankingsResponseDto(Year year, int week,
                                            List<WeeklyRankingResponseDto> weeklyRankings) {

    public FullWeeklyRankingsResponseDto(List<WeeklyRanking> weeklyRankings) {
        this(
                weeklyRankings.isEmpty() ? null : Year.of(weeklyRankings.getFirst().week().year()),
                weeklyRankings.isEmpty() ? 0 : weeklyRankings.getFirst().week().number(),
                weeklyRankings.stream().map(WeeklyRankingResponseDto::new).toList()
        );
    }
}