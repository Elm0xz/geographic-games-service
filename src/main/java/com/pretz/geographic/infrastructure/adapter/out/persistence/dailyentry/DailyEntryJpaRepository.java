package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    @EntityGraph(attributePaths = {"game", "player"})
    List<DailyEntryJpaEntity> findByGame_IdInAndEntryDateIn(Collection<Long> gameIds, Collection<LocalDate> entryDates);

    /**
     * Guarded update: optimistic locking on {@code submittedAt}. A batch reads persisted entries and writes its
     * corrections in two separate transactions, so a concurrent batch may change the entry in between. The update
     * applies only if the stored entry is still submitted later than the correction.
     *
     * @return 1 if the correction was applied, 0 if a concurrent change made it obsolete
     */
    @Modifying
    @Query("""
            update DailyEntryJpaEntity e
               set e.points = :points, e.submittedAt = :submittedAt
             where e.id = :id and e.submittedAt > :submittedAt""")
    int correctIfSubmittedEarlier(@Param("id") Long id, @Param("points") int points, @Param("submittedAt") Instant submittedAt);
}
