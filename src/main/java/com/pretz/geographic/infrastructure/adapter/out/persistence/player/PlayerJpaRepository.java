package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerJpaRepository extends JpaRepository<PlayerJpaEntity, Long> {
}
