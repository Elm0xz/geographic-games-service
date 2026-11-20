package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.DailyRanking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public record DailyRankingsReportResponseDto(LocalDate from, LocalDate to,
                                             List<DailyRankingReportResponseDto> dailyRankings) {

    public DailyRankingsReportResponseDto(LocalDate from, LocalDate to, Collection<DailyRanking> dailyRankings) {
        this(from, to, dailyRankings.stream().map(DailyRankingReportResponseDto::new).toList());
    }
}
