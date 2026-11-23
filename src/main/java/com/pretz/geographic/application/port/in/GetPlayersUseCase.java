package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;

import java.util.List;

public interface GetPlayersUseCase {

    List<Player> getAllPlayers();

    Player getPlayer(PlayerId playerId);
}
