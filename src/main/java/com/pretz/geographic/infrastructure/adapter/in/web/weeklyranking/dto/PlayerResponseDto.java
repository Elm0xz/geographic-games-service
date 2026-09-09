package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto;

import com.pretz.geographic.application.domain.model.Player;

public record PlayerResponseDto(Long playerId, String name) {

    public PlayerResponseDto(Player player) {
        this(player.playerId().id(), player.name());
    }
}