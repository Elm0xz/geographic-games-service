package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveWeeklyRankingPort;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class WeeklyRankingPersistenceAdapter implements LoadWeeklyRankingPort, SaveWeeklyRankingPort {

    private final WeeklyRankingJpaRepository weeklyRankingRepository;
    private final GameJpaRepository gameRepository;
    private final PlayerJpaRepository playerRepository;
    private final WeeklyRankingPersistenceMapper mapper;

    WeeklyRankingPersistenceAdapter(WeeklyRankingJpaRepository weeklyRankingRepository,
                                    GameJpaRepository gameRepository,
                                    PlayerJpaRepository playerRepository,
                                    WeeklyRankingPersistenceMapper mapper) {
        this.weeklyRankingRepository = weeklyRankingRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyRanking> loadWeeklyRankings(List<Game> games, Week week) {
        return weeklyRankingRepository.findByGame_IdInAndYearAndWeek(
                        games.stream().map(g -> g.gameId().id()).toList(),
                        week.year(),
                        week.number())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<WeeklyRanking> save(List<WeeklyRanking> rankings) {
        return rankings.stream()
                .map(ranking -> mapper.toDomain(
                        weeklyRankingRepository.save(mapper.toEntity(
                                ranking,
                                gameRepository::getReferenceById,
                                playerRepository::getReferenceById
                        ))))
                .toList();
    }
}
