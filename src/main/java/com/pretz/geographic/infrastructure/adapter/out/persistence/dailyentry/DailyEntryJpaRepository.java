package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DailyEntryJpaRepository extends JpaRepository<DailyEntryJpaEntity, Long> {

    @EntityGraph(attributePaths = {"game", "player"})
    List<DailyEntryJpaEntity> findByGame_IdAndEntryDate(Long gameId, LocalDate entryDate);

    @EntityGraph(attributePaths = {"game", "player"})
    List<DailyEntryJpaEntity> findByGame_IdInAndEntryDate(Collection<Long> gameIds, LocalDate entryDate);
}
