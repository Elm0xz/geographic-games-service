package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WeeklyRankingJpaRepository extends JpaRepository<WeeklyRankingJpaEntity, Long> {

    /**
     * Finds rankings of given games for all ISO weeks between {@code (fromYear, fromWeek)} and
     * {@code (toYear, toWeek)}, both inclusive. The interval may span several years.
     */
    @EntityGraph(attributePaths = {"game", "positions", "positions.player"})
    @Query("""
            select r from WeeklyRankingJpaEntity r
             where r.game.id in :gameIds
               and (r.year > :fromYear or (r.year = :fromYear and r.week >= :fromWeek))
               and (r.year < :toYear or (r.year = :toYear and r.week <= :toWeek))""")
    List<WeeklyRankingJpaEntity> findByGameIdsAndWeekInterval(@Param("gameIds") Collection<Long> gameIds,
                                                              @Param("fromYear") int fromYear,
                                                              @Param("fromWeek") int fromWeek,
                                                              @Param("toYear") int toYear,
                                                              @Param("toWeek") int toWeek);

    List<WeeklyRankingJpaEntity> findByGame_IdInAndYearInAndWeekIn(Collection<Long> gameIds, Collection<Integer> years, Collection<Integer> weeks);
}
