package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.out.LoadGamePort;
import org.springframework.stereotype.Component;

@Component
public class GamePersistenceAdapter implements LoadGamePort {

    @Override
    public Game loadGame(Long id) {
        return null;
    }
}
