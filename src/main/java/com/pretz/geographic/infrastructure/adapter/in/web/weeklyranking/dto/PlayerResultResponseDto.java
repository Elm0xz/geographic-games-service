package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.WeeklyPosition;

public record PlayerResultResponseDto(PlayerResponseDto player, int wins, float points) {

    public PlayerResultResponseDto(WeeklyPosition position) {
        this(new PlayerResponseDto(position.player()), position.wins(), position.points());
    }
}