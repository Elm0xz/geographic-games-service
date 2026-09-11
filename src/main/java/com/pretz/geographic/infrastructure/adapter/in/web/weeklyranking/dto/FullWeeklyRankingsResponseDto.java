package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.time.Year;
import java.util.Collection;
import java.util.List;

public record FullWeeklyRankingsResponseDto(Year year, int week,
                                            List<WeeklyRankingResponseDto> weeklyRankings) {

    public FullWeeklyRankingsResponseDto(Year year, int week, Collection<WeeklyRanking> weeklyRankings) {
        this(
                year,
                week,
                weeklyRankings.stream().map(WeeklyRankingResponseDto::new).toList()
        );
    }
}