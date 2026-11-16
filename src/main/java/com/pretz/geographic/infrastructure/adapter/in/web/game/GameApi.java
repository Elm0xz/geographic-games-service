package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.GameResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/games")
public interface GameApi {

    /**
     * Returns all games registered in the system, sorted alphabetically by name.
     */
    @GetMapping
    ResponseEntity<List<GameResponseDto>> getAllGames();
}
