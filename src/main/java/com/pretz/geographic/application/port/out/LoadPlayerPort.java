package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Player;

import java.util.List;

public interface LoadPlayerPort {

    Player loadPlayer(Long id);

    Player loadPlayer(String name);

    List<Player> loadPlayers(List<Long> ids);
}
