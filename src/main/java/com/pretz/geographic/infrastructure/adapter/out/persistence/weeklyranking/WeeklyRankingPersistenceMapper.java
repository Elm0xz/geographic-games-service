package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GamePersistenceMapper;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class WeeklyRankingPersistenceMapper {

    private final GamePersistenceMapper gamePersistenceMapper;
    private final PlayerPersistenceMapper playerPersistenceMapper;

    public WeeklyRankingPersistenceMapper(GamePersistenceMapper gamePersistenceMapper,
                                          PlayerPersistenceMapper playerPersistenceMapper) {
        this.gamePersistenceMapper = gamePersistenceMapper;
        this.playerPersistenceMapper = playerPersistenceMapper;
    }

    public WeeklyRanking toDomain(WeeklyRankingJpaEntity entity) {
        var game = gamePersistenceMapper.toDomain(entity.getGame());
        var week = new Week(entity.getYear(), entity.getWeek());
        List<WeeklyPosition> positions = entity.getPositions().stream()
                .map(pos -> new WeeklyPosition(
                        game,
                        week,
                        playerPersistenceMapper.toDomain(pos.getPlayer()),
                        pos.getWins(),
                        pos.getPoints()))
                .toList();
        return new WeeklyRanking(game, week, positions);
    }

    //TODO [GEOG-10] playerLookup?
    public WeeklyRankingJpaEntity toEntity(WeeklyRanking ranking,
                                           GameJpaEntity gameEntity,
                                           Function<Long, PlayerJpaEntity> playerLookup) {
        WeeklyRankingJpaEntity rankingEntity = new WeeklyRankingJpaEntity(
                gameEntity,
                ranking.week().year(),
                ranking.week().number(),
                new ArrayList<>()
        );
        List<WeeklyPositionJpaEntity> positionEntities = ranking.positions().stream()
                .map(pos -> new WeeklyPositionJpaEntity(
                        rankingEntity,
                        playerLookup.apply(pos.player().playerId().id()),
                        pos.wins(),
                        (int) pos.points()))
                .toList();
        rankingEntity.setPositions(positionEntities);
        return rankingEntity;
    }
}
