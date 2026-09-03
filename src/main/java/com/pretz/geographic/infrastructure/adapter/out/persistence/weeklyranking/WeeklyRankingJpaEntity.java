
package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.List;

@Entity
@Table(name = "weekly_ranking", uniqueConstraints = @UniqueConstraint(
        name = "uq_weekly_ranking_game_year_week",
        columnNames = {"game_id", "year", "week"}))
public class WeeklyRankingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameJpaEntity game;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int week;

    @OneToMany(mappedBy = "weeklyRanking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyPositionJpaEntity> positions;

    protected WeeklyRankingJpaEntity() {
    }

    public WeeklyRankingJpaEntity(GameJpaEntity game, int year, int week, List<WeeklyPositionJpaEntity> positions) {
        this.game = game;
        this.year = year;
        this.week = week;
        this.positions = positions;
    }

    public void setPositions(List<WeeklyPositionJpaEntity> positions) {
        this.positions = positions;
    }

    public Long getId() { return id; }
    public GameJpaEntity getGame() { return game; }
    public int getYear() { return year; }
    public int getWeek() { return week; }
    public List<WeeklyPositionJpaEntity> getPositions() { return positions; }
}