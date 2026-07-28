package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameJpaRepository extends JpaRepository<GameJpaEntity, Long> {

    Optional<GameJpaEntity> findByName(String name);
}
