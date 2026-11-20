package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.WeeklyPosition;

public record PlayerResultReportResponseDto(PlayerResponseDto player, int wins, float points) {

    public PlayerResultReportResponseDto(WeeklyPosition position) {
        this(new PlayerResponseDto(position.player()), position.wins(), position.points());
    }
}
