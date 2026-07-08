package com.pretz.geographic.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface GameJpaRepository extends JpaRepository<GameJpaEntity, Long> {

    Optional<GameJpaEntity> findByName(String name);
}
