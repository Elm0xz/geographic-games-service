package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.port.in.CreateGameUseCase;
import com.pretz.geographic.application.port.in.GetGamesUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.CreateGameRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.GameResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
public class GameController implements GameApi {

    private final GetGamesUseCase getGamesUseCase;
    private final CreateGameUseCase createGameUseCase;

    public GameController(GetGamesUseCase getGamesUseCase, CreateGameUseCase createGameUseCase) {
        this.getGamesUseCase = getGamesUseCase;
        this.createGameUseCase = createGameUseCase;
    }

    @Override
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        return ResponseEntity.ok(getGamesUseCase.getAllGames().stream()
                .map(GameResponseDto::new).toList());
    }

    @Override
    public ResponseEntity<GameResponseDto> getGame(Long gameId) {
        return ResponseEntity.ok(new GameResponseDto(getGamesUseCase.getGame(new GameId(gameId))));
    }

    @Override
    public ResponseEntity<GameResponseDto> createGame(CreateGameRequestDto createGameRequestDto) {
        var game = createGameUseCase.createGame(createGameRequestDto.toCommand());
        var location = fromMethodCall(on(GameApi.class).getGame(game.gameId().id())).build().toUri();
        return ResponseEntity.created(location).body(new GameResponseDto(game));
    }
}
