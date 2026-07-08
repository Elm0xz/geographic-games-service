package com.pretz.geographic.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface DailyEntryJpaRepository extends JpaRepository<DailyEntryJpaEntity, Long> {

    List<DailyEntryJpaEntity> findByGame_NameAndEntryDateBetween(String gameName, LocalDate from, LocalDate to);
}
