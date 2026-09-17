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

    @EntityGraph(attributePaths = {"game", "player"})
    List<DailyEntryJpaEntity> findByGame_IdInAndEntryDateBetween(Collection<Long> gameIds, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"game", "player"})//TODO maybe we can drop player nere?
    List<DailyEntryJpaEntity> findByGame_IdInAndEntryDateIn(Collection<Long> gameIds, Collection<LocalDate> entryDates);
}
