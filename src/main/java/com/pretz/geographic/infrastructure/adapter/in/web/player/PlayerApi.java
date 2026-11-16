package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.PlayerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/players")
public interface PlayerApi {

    /**
     * Returns all players registered in the system, sorted alphabetically by name.
     */
    @GetMapping
    ResponseEntity<List<PlayerResponseDto>> getAllPlayers();
}
