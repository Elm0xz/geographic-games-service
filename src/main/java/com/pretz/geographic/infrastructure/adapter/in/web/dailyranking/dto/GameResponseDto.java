package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto;

import com.pretz.geographic.application.domain.model.Game;

public record GameResponseDto(Long gameId, String name) {

    public GameResponseDto(Game game) {
        this(game.gameId().id(), game.name());
    }
}
