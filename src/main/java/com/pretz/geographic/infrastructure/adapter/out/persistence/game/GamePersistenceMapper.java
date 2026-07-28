package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import org.springframework.stereotype.Component;

@Component
public class GamePersistenceMapper {
    public GamePersistenceMapper() {
    }

    public Game toDomain(GameJpaEntity entity) {
        return new Game(new GameId(entity.getId()), entity.getName(), entity.getScoringSystem());
    }
}