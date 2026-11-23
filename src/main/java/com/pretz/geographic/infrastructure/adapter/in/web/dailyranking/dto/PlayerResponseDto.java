package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto;

import com.pretz.geographic.application.domain.model.Player;

public record PlayerResponseDto(Long id, String name) {

    public PlayerResponseDto(Player player) {
        this(player.playerId().id(), player.name());
    }
}