package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Player;

public interface CreatePlayerUseCase {

    Player createPlayer(CreatePlayerCommand command);
}
