package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.CreatePlayerRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.PlayerResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/players")
public interface PlayerApi {

    /**
     * Returns all players registered in the system, sorted alphabetically by name.
     */
    @GetMapping
    ResponseEntity<List<PlayerResponseDto>> getAllPlayers();

    /**
     * Returns the player with the given id, or {@code 404 NOT_FOUND} when it doesn't exist.
     */
    @GetMapping("/{playerId}")
    ResponseEntity<PlayerResponseDto> getPlayer(@PathVariable Long playerId);

    /**
     * Creates a player. Admin only.
     *
     * <h2>Responses</h2>
     * <ul>
     *     <li><b>201 Created</b>: the created player, with its URI in the {@code Location} header.</li>
     *     <li><b>400 Bad Request</b> ({@code VALIDATION_ERROR}, {@code MALFORMED_REQUEST}): the name is blank, longer
     *     than 50 characters or contains characters other than letters, digits, spaces and {@code _ . ' -} (it must
     *     start and end with a letter or digit).</li>
     *     <li><b>409 Conflict</b> ({@code RESOURCE_ALREADY_EXISTS}): a player with the same name already exists.</li>
     * </ul>
     */
    @PostMapping
    ResponseEntity<PlayerResponseDto> createPlayer(@Valid @RequestBody CreatePlayerRequestDto createPlayerRequestDto);
}
