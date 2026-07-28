package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PlayerJpaRepository extends JpaRepository<PlayerJpaEntity, Long> {

    Optional<PlayerJpaEntity> findByName(String name);
}
