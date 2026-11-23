package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.exception.PlayerAlreadyExistsException;

public interface SavePlayerPort {

    /**
     * Persists a new player and returns it with its generated id.
     *
     * @throws PlayerAlreadyExistsException when a player with the same name already exists
     */
    Player save(Player player);
}
