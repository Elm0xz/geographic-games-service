package com.pretz.geographic.infrastructure.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.springframework.stereotype.Component;

@Component
class DailyEntryPersistenceMapper {

    DailyEntry toDomain(DailyEntryJpaEntity entity) {
        return new DailyEntry(
                toDomain(entity.getGame()),
                entity.getEntryDate(),
                new Player(entity.getPlayer().getName()),
                entity.getPoints());
    }

    Game toDomain(GameJpaEntity entity) {
        return new Game(entity.getName(), ScoringSystem.valueOf(entity.getScoringSystem()));
    }
}
