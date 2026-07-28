package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
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

import java.time.LocalDate;

@Entity
@Table(name = "daily_entry", uniqueConstraints = @UniqueConstraint(
        name = "uq_daily_entry_game_player_date",
        columnNames = {"game_id", "player_id", "entry_date"}))
public class DailyEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameJpaEntity game;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerJpaEntity player;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false)
    private int points;

    protected DailyEntryJpaEntity() {
    }

    public DailyEntryJpaEntity(GameJpaEntity game, PlayerJpaEntity player, LocalDate entryDate, int points) {
        this.game = game;
        this.player = player;
        this.entryDate = entryDate;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public GameJpaEntity getGame() {
        return game;
    }

    public PlayerJpaEntity getPlayer() {
        return player;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public int getPoints() {
        return points;
    }
}
