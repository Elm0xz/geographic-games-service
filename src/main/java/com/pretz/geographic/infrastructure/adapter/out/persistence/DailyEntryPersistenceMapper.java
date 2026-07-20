package com.pretz.geographic.infrastructure.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import org.springframework.stereotype.Component;

@Component
class DailyEntryPersistenceMapper {

    DailyEntry toDomain(DailyEntryJpaEntity entity) {
        return new DailyEntry(
                new DailyEntryId(1L), toDomain(entity.getGame()),
                entity.getEntryDate(),
                toDomain(entity.getPlayer()),
                entity.getPoints());
    }

    private Player toDomain(PlayerJpaEntity entity) {
        return new Player(new PlayerId(1L), entity.getName());
    }

    private Game toDomain(GameJpaEntity entity) {
        return new Game(new GameId(1L), entity.getName(), entity.getScoringSystem());
    }
}
