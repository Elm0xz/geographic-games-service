package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.validation.PlayerNotFoundException;
import com.pretz.geographic.application.port.in.CreatePlayerCommand;
import com.pretz.geographic.application.port.in.CreatePlayerUseCase;
import com.pretz.geographic.application.port.in.GetPlayersUseCase;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SavePlayerPort;

import java.util.Comparator;
import java.util.List;

public class PlayerService implements GetPlayersUseCase, CreatePlayerUseCase {

    private static final Comparator<Player> BY_NAME = Comparator.comparing(Player::name);

    private final LoadPlayerPort loadPlayerPort;
    private final SavePlayerPort savePlayerPort;

    public PlayerService(LoadPlayerPort loadPlayerPort, SavePlayerPort savePlayerPort) {
        this.loadPlayerPort = loadPlayerPort;
        this.savePlayerPort = savePlayerPort;
    }

    @Override
    public List<Player> getAllPlayers() {
        return loadPlayerPort.loadAllPlayers().stream()
                .sorted(BY_NAME)
                .toList();
    }

    @Override
    public Player getPlayer(PlayerId playerId) {
        return loadPlayerPort.loadPlayers(List.of(playerId.id())).stream()
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    @Override
    public Player createPlayer(CreatePlayerCommand command) {
        return savePlayerPort.save(new Player(null, command.name()));
    }
}
