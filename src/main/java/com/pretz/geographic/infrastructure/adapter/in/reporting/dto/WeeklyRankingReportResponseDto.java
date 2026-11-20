package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public record WeeklyRankingReportResponseDto(GameResponseDto game, WeekResponseDto week,
                                             List<PlayerResultReportResponseDto> playerResults) {

    public WeeklyRankingReportResponseDto(WeeklyRanking ranking) {
        this(new GameResponseDto(ranking.game()), new WeekResponseDto(ranking.week()),
                ranking.positions().stream().map(PlayerResultReportResponseDto::new).toList());
    }
}
