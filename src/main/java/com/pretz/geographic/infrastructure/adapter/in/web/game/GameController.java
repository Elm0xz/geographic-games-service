package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.application.port.in.GetGamesUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.game.dto.GameResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController implements GameApi {

    private final GetGamesUseCase getGamesUseCase;

    public GameController(GetGamesUseCase getGamesUseCase) {
        this.getGamesUseCase = getGamesUseCase;
    }

    @Override
    public ResponseEntity<List<GameResponseDto>> getAllGames() {
        return ResponseEntity.ok(getGamesUseCase.getAllGames().stream()
                .map(GameResponseDto::new).toList());
    }
}
