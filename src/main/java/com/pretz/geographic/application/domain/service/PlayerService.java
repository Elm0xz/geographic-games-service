package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.in.GetPlayersUseCase;
import com.pretz.geographic.application.port.out.LoadPlayerPort;

import java.util.Comparator;
import java.util.List;

public class PlayerService implements GetPlayersUseCase {

    private static final Comparator<Player> BY_NAME = Comparator.comparing(Player::name);

    private final LoadPlayerPort loadPlayerPort;

    public PlayerService(LoadPlayerPort loadPlayerPort) {
        this.loadPlayerPort = loadPlayerPort;
    }

    @Override
    public List<Player> getAllPlayers() {
        return loadPlayerPort.loadAllPlayers().stream()
                .sorted(BY_NAME)
                .toList();
    }
}
