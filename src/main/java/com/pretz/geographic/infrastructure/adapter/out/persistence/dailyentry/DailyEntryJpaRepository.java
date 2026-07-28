package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface DailyEntryJpaRepository extends JpaRepository<DailyEntryJpaEntity, Long> {

    List<DailyEntryJpaEntity> findByGame_NameAndEntryDate(String gameName, LocalDate date);
}
