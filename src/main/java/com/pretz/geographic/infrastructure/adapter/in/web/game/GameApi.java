package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.CreateGameRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.GameResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/games")
public interface GameApi {

    /**
     * Returns all games registered in the system, sorted alphabetically by name.
     */
    @GetMapping
    ResponseEntity<List<GameResponseDto>> getAllGames();

    /**
     * Returns the game with the given id, or {@code 404 NOT_FOUND} when it doesn't exist.
     */
    @GetMapping("/{gameId}")
    ResponseEntity<GameResponseDto> getGame(@PathVariable Long gameId);

    /**
     * Creates a game. Admin only. The scoring system defaults to {@code STANDARD} when omitted.
     *
     * <h2>Responses</h2>
     * <ul>
     *     <li><b>201 Created</b>: the created game, with its URI in the {@code Location} header.</li>
     *     <li><b>400 Bad Request</b> ({@code VALIDATION_ERROR}, {@code MALFORMED_REQUEST}): the name is blank, longer
     *     than 50 characters or contains characters other than letters, digits, spaces and {@code _ . ' -} (it must
     *     start and end with a letter or digit), or the scoring system is unknown.</li>
     *     <li><b>409 Conflict</b> ({@code RESOURCE_ALREADY_EXISTS}): a game with the same name already exists.</li>
     * </ul>
     */
    @PostMapping
    ResponseEntity<GameResponseDto> createGame(@Valid @RequestBody CreateGameRequestDto createGameRequestDto);
}
