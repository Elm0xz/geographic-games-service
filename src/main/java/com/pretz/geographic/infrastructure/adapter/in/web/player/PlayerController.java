package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.port.in.CreatePlayerUseCase;
import com.pretz.geographic.application.port.in.GetPlayersUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.CreatePlayerRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.player.dto.PlayerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
public class PlayerController implements PlayerApi {

    private final GetPlayersUseCase getPlayersUseCase;
    private final CreatePlayerUseCase createPlayerUseCase;

    public PlayerController(GetPlayersUseCase getPlayersUseCase, CreatePlayerUseCase createPlayerUseCase) {
        this.getPlayersUseCase = getPlayersUseCase;
        this.createPlayerUseCase = createPlayerUseCase;
    }

    @Override
    public ResponseEntity<List<PlayerResponseDto>> getAllPlayers() {
        return ResponseEntity.ok(getPlayersUseCase.getAllPlayers().stream()
                .map(PlayerResponseDto::new).toList());
    }

    @Override
    public ResponseEntity<PlayerResponseDto> getPlayer(Long playerId) {
        return ResponseEntity.ok(new PlayerResponseDto(getPlayersUseCase.getPlayer(new PlayerId(playerId))));
    }

    @Override
    public ResponseEntity<PlayerResponseDto> createPlayer(CreatePlayerRequestDto createPlayerRequestDto) {
        var player = createPlayerUseCase.createPlayer(createPlayerRequestDto.toCommand());
        var location = fromMethodCall(on(PlayerApi.class).getPlayer(player.playerId().id())).build().toUri();
        return ResponseEntity.created(location).body(new PlayerResponseDto(player));
    }
}
