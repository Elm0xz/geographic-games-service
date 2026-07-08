package com.pretz.geographic.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DailyEntryPersistenceAdapter.class, DailyEntryPersistenceMapper.class})
@Testcontainers
class DailyEntryPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private DailyEntryPersistenceAdapter adapter;

    @Autowired
    private GameJpaRepository gameRepository;

    @Autowired
    private PlayerJpaRepository playerRepository;

    private final Game game = new Game("Worldle", ScoringSystem.STANDARD);
    private final Player player = new Player("Player1");
    private final LocalDate date = LocalDate.of(2026, 6, 30);

    @Test
    void shouldSaveAndLoadEntryWithinDateRange() {

        //given
        var entry = new DailyEntry(game, date, player, 900);

        //when
        var saved = adapter.save(entry);
        var loaded = adapter.loadEntries(game, date, date);

        //then
        assertThat(saved).isEqualTo(entry);
        assertThat(loaded).containsExactly(entry);
    }

    @Test
    void shouldNotLoadEntriesOutsideDateRange() {

        //given
        adapter.save(new DailyEntry(game, date, player, 900));

        //when
        var loaded = adapter.loadEntries(game, date.plusDays(1), date.plusDays(7));

        //then
        assertThat(loaded).isEmpty();
    }

    @Test
    void shouldReuseGameAndPlayerRowsAcrossSaves() {

        //when
        adapter.save(new DailyEntry(game, date, player, 900));
        adapter.save(new DailyEntry(game, date.plusDays(1), player, 820));

        //then
        assertThat(gameRepository.count()).isEqualTo(1);
        assertThat(playerRepository.count()).isEqualTo(1);
    }
}
