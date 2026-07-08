package com.pretz.geographic.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
class GameJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "scoring_system", nullable = false)
    private String scoringSystem;

    protected GameJpaEntity() {
    }

    GameJpaEntity(String name, String scoringSystem) {
        this.name = name;
        this.scoringSystem = scoringSystem;
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getScoringSystem() {
        return scoringSystem;
    }
}
