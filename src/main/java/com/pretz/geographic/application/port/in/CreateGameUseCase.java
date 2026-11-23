package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.Game;

public interface CreateGameUseCase {

    Game createGame(CreateGameCommand command);
}
