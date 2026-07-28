package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import org.springframework.stereotype.Component;

@Component
public class PlayerPersistenceAdapter implements LoadPlayerPort {

    @Override
    public Player loadPlayer(Long id) {
        return null;
    }

    @Override
    public Player loadPlayer(String name) {
        return null;
    }
}
