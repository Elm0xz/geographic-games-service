package com.pretz.geographic.infrastructure.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_system", nullable = false)
    private ScoringSystem scoringSystem;

    protected GameJpaEntity() {
    }

    GameJpaEntity(String name, ScoringSystem scoringSystem) {
        this.name = name;
        this.scoringSystem = scoringSystem;
    }

    Long getId() {
        return id;
    }

    String getName() {
        return name;
    }

    ScoringSystem getScoringSystem() {
        return scoringSystem;
    }
}
