package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.DailyRanking;

import java.time.LocalDate;
import java.util.List;

public record DailyRankingReportResponseDto(GameResponseDto game, LocalDate date,
                                            List<DailyEntryReportResponseDto> entries) {

    public DailyRankingReportResponseDto(DailyRanking dailyRanking) {
        this(new GameResponseDto(dailyRanking.game()), dailyRanking.date(),
                dailyRanking.entries().stream().map(DailyEntryReportResponseDto::new).toList());
    }
}
