package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.application.port.in.GetPlayersUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.PlayerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlayerController implements PlayerApi {

    private final GetPlayersUseCase getPlayersUseCase;

    public PlayerController(GetPlayersUseCase getPlayersUseCase) {
        this.getPlayersUseCase = getPlayersUseCase;
    }

    @Override
    public ResponseEntity<List<PlayerResponseDto>> getAllPlayers() {
        return ResponseEntity.ok(getPlayersUseCase.getAllPlayers().stream()
                .map(PlayerResponseDto::new).toList());
    }
}
