package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface GameJpaRepository extends JpaRepository<GameJpaEntity, Long> {

    Optional<GameJpaEntity> findByName(String name);
}
