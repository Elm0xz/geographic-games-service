package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "weekly_position", uniqueConstraints = @UniqueConstraint(
        name = "uq_weekly_position_weekly_ranking_player",
        columnNames = {"weekly_ranking_id", "player_id"}))
public class WeeklyPositionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "weekly_ranking_id", nullable = false)
    private WeeklyRankingJpaEntity weeklyRanking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerJpaEntity player;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private float points;

    protected WeeklyPositionJpaEntity() {
    }

    public WeeklyPositionJpaEntity(WeeklyRankingJpaEntity weeklyRanking, PlayerJpaEntity player, int wins, int points) {
        this.weeklyRanking = weeklyRanking;
        this.player = player;
        this.wins = wins;
        this.points = points;
    }

    public Long getId() { return id; }
    public WeeklyRankingJpaEntity getWeeklyRanking() { return weeklyRanking; }
    public PlayerJpaEntity getPlayer() { return player; }
    public int getWins() { return wins; }
    public float getPoints() { return points; }
}
