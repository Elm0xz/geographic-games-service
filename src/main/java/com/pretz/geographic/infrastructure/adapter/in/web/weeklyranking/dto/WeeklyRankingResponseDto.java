package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import java.util.List;

public record WeeklyRankingResponseDto(GameResponseDto game, List<PlayerResultResponseDto> playerResults) {
}
