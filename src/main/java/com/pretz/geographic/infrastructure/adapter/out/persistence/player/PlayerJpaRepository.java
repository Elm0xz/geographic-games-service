package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerJpaRepository extends JpaRepository<PlayerJpaEntity, Long> {

    Optional<PlayerJpaEntity> findByName(String name);
}
