package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.out.exception.GameAlreadyExistsException;

public interface SaveGamePort {

    /**
     * Persists a new game and returns it with its generated id.
     *
     * @throws GameAlreadyExistsException when a game with the same name already exists
     */
    Game save(Game game);
}
