package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.WeeklyRanking;

import java.util.List;

public record WeeklyRankingResponseDto(GameResponseDto game, List<PlayerResultResponseDto> playerResults) {

    public WeeklyRankingResponseDto(WeeklyRanking ranking) {
        this(
                new GameResponseDto(ranking.game()),
                ranking.positions().stream().map(PlayerResultResponseDto::new).toList()
        );
    }
}