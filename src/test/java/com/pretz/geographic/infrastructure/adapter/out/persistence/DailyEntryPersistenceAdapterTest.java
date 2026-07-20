package com.pretz.geographic.infrastructure.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DailyEntryPersistenceAdapter.class, DailyEntryPersistenceMapper.class})
@Testcontainers
class DailyEntryPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    @Autowired
    private DailyEntryPersistenceAdapter adapter;

    @Autowired
    private GameJpaRepository gameRepository;

    @Autowired
    private PlayerJpaRepository playerRepository;

    private final Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
    private final Player player = new Player(new PlayerId(1L), "Player1");
    private final LocalDate date = LocalDate.of(2026, 6, 30);

    @Test
    void shouldSaveAndLoadEntryOnRequestedDate() {

        //given
        var entry = new DailyEntry(new DailyEntryId(1L), game, date, player, 900);

        //when
        var saved = adapter.save(entry);
        var loaded = adapter.loadEntries(game, date);

        //then
        assertThat(saved).isEqualTo(entry);
        assertThat(loaded).containsExactly(entry);
    }

    @Test
    void shouldNotLoadEntriesOutsideDateRange() {

        //given
        adapter.save(new DailyEntry(new DailyEntryId(1L), game, date, player, 900));

        //when
        var loaded = adapter.loadEntries(game, date.plusDays(1));

        //then
        assertThat(loaded).isEmpty();
    }

    @Test
    void shouldReuseGameAndPlayerRowsAcrossSaves() {

        //when
        adapter.save(new DailyEntry(new DailyEntryId(1L), game, date, player, 900));
        adapter.save(new DailyEntry(new DailyEntryId(2L), game, date.plusDays(1), player, 820));

        //then
        assertThat(gameRepository.count()).isEqualTo(1);
        assertThat(playerRepository.count()).isEqualTo(1);
    }
}
