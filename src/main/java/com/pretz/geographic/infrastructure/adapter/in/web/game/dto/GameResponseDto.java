package com.pretz.geographic.infrastructure.adapter.in.web.game.dto;

import com.pretz.geographic.application.domain.model.Game;

public record GameResponseDto(Long id, String name) {

    public GameResponseDto(Game game) {
        this(game.gameId().id(), game.name());
    }
}
