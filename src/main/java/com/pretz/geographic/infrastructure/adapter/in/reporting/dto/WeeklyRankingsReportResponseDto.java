package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.Collection;
import java.util.List;

public record WeeklyRankingsReportResponseDto(WeekResponseDto from, WeekResponseDto to,
                                              List<WeeklyRankingReportResponseDto> weeklyRankings) {

    public WeeklyRankingsReportResponseDto(Week from, Week to, Collection<WeeklyRanking> weeklyRankings) {
        this(new WeekResponseDto(from), new WeekResponseDto(to),
                weeklyRankings.stream().map(WeeklyRankingReportResponseDto::new).toList());
    }
}
