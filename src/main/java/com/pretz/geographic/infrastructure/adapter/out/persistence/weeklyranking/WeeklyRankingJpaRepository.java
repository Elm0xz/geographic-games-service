package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRankingJpaEntity, Long> {

    @EntityGraph(attributePaths = {"game", "positions", "positions.player"})
    List<WeeklyRankingJpaEntity> findByGame_IdInAndYearAndWeek(Collection<Long> gameIds, int year, int week);
}
