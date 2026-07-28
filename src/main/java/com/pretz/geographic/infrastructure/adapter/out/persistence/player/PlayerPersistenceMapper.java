package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import org.springframework.stereotype.Component;

@Component
public class PlayerPersistenceMapper {
    public PlayerPersistenceMapper() {
    }

    public Player toDomain(PlayerJpaEntity entity) {
        return new Player(new PlayerId(entity.getId()), entity.getName());
    }
}