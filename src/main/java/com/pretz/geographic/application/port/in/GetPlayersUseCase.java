package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Player;

import java.util.List;

public interface GetPlayersUseCase {

    List<Player> getAllPlayers();
}
