package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GamePersistenceMapper;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DailyEntryPersistenceAdapter.class, DailyEntryPersistenceMapper.class, GamePersistenceMapper.class, PlayerPersistenceMapper.class})
@Testcontainers
class DailyEntryPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    @Autowired
    private DailyEntryPersistenceAdapter adapter;

    @Autowired
    private GamePersistenceMapper gamePersistenceMapper;

    @Autowired
    private PlayerPersistenceMapper playerPersistenceMapper;

    @Autowired
    private GameJpaRepository gameRepository;

    @Autowired
    private PlayerJpaRepository playerRepository;

    @Autowired
    private DailyEntryJpaRepository dailyEntryJpaRepository;

    private final Game game = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player = new Player(null, "Player1");
    private final LocalDate date = LocalDate.of(2026, 6, 30);
    private Game savedGame;
    private Player savedPlayer;

    @BeforeEach
    void saveGameAndPlayer() {
        cleanup();
        savedGame = gamePersistenceMapper.toDomain(gameRepository.save(new GameJpaEntity(game.name(), game.scoringSystem())));
        savedPlayer = playerPersistenceMapper.toDomain(playerRepository.save(new PlayerJpaEntity(player.name())));
    }

    @Test
    void shouldSaveAndLoadEntryOnRequestedDate() {

        //given
        var entry = new DailyEntry(null, savedGame, date, savedPlayer, 900);

        //when
        var saved = adapter.save(entry);
        var loaded = adapter.loadEntries(savedGame, date);

        //then
        assertIdIsNotNull(saved);
        assertEqualsIgnoringId(saved, entry);
        assertThat(loaded).containsExactly(saved);
    }

    @Test
    void shouldNotLoadEntriesOutsideDateRange() {

        //given
        adapter.save(new DailyEntry(null, savedGame, date, savedPlayer, 900));

        //when
        var loaded = adapter.loadEntries(savedGame, date.plusDays(1));

        //then
        assertThat(loaded).isEmpty();
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownGameId() {
        // given
        var unknownGame = new Game(new GameId(999L), "Unknown", ScoringSystem.STANDARD);
        var entry = new DailyEntry(null, unknownGame, date, savedPlayer, 900);

        // when / then
        assertThatThrownBy(() -> adapter.save(entry))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownPlayerId() {
        // given
        var unknownPlayer = new Player(new PlayerId(999L), "Unknown");
        var entry = new DailyEntry(null, savedGame, date, unknownPlayer, 900);

        // when / then
        assertThatThrownBy(() -> adapter.save(entry))
                .isInstanceOf(DataAccessException.class);
    }

    private void assertIdIsNotNull(DailyEntry savedEntry) {
        assertThat(savedEntry.dailyEntryId()).isNotNull();
        assertThat(savedEntry.dailyEntryId().id()).isNotNull();
    }

    private void assertEqualsIgnoringId(DailyEntry savedEntry, DailyEntry inputEntry) {
        assertThat(savedEntry)
                .usingRecursiveComparison()
                .ignoringFields("dailyEntryId")
                .isEqualTo(inputEntry);
    }

    private void cleanup() {
        dailyEntryJpaRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
    }
}
