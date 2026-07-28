package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GamePersistenceMapper;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerPersistenceMapper;
import org.springframework.stereotype.Component;

@Component
public class DailyEntryPersistenceMapper {

    private final PlayerPersistenceMapper playerPersistenceMapper = new PlayerPersistenceMapper();
    private final GamePersistenceMapper gamePersistenceMapper = new GamePersistenceMapper();

    public DailyEntry toDomain(DailyEntryJpaEntity entity) {
        return new DailyEntry(
                new DailyEntryId(entity.getId()),
                gamePersistenceMapper.toDomain(entity.getGame()),
                entity.getEntryDate(),
                playerPersistenceMapper.toDomain(entity.getPlayer()),
                entity.getPoints());
    }
}
