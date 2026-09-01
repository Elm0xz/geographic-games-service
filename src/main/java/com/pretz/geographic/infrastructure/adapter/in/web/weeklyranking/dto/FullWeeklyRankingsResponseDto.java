package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.time.Year;
import java.util.List;

public record FullWeeklyRankingsResponseDto(Year year, int week,
                                            List<WeeklyRankingResponseDto> weeklyRankingResponseDtoList) {

    public FullWeeklyRankingsResponseDto(List<WeeklyRanking> weeklyRankings) {
        this(null, 0, null); //TODO implement
    }
}
