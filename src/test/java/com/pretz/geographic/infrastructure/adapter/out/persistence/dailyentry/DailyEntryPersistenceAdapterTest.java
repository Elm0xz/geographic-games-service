package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
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
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DailyEntryPersistenceAdapter.class, DailyEntryPersistenceMapper.class, GamePersistenceMapper.class, PlayerPersistenceMapper.class})
class DailyEntryPersistenceAdapterTest extends AbstractPostgresDataJpaTest {

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

    private final Game game1 = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player1 = new Player(null, "Player1");
    private final Game game2 = new Game(null, "WhenTaken", ScoringSystem.STANDARD);
    private final Player player2 = new Player(null, "Player2");
    private final LocalDate date = LocalDate.of(2026, 6, 30);
    private List<Game> savedGames;
    private List<Player> savedPlayers;

    @BeforeEach
    void saveGamesAndPlayers() {
        savedGames = gameRepository.saveAll(List.of(
                        new GameJpaEntity(game1.name(), game1.scoringSystem()),
                        new GameJpaEntity(game2.name(), game2.scoringSystem())))
                .stream().map(it -> gamePersistenceMapper.toDomain(it))
                .toList();
        savedPlayers = playerRepository.saveAll(List.of(
                        new PlayerJpaEntity(player1.name()),
                        new PlayerJpaEntity(player2.name())))
                .stream().map(it -> playerPersistenceMapper.toDomain(it))
                .toList();
        ;
    }

    @Test
    void shouldSaveAndLoadEntriesOnRequestedDate() {

        //given
        var entry = new DailyEntry(null, getGame1(), date, getPlayer1(), 900);

        //when
        var saved = adapter.save(entry);
        var loaded = adapter.loadEntries(savedGames, date);

        //then
        assertIdIsNotNull(saved);
        assertEqualsIgnoringId(saved, entry);
        assertThat(loaded).containsExactly(saved);
    }

    @Test
    void shouldNotLoadEntriesOutsideDateRange() {

        //given
        adapter.save(new DailyEntry(null, getGame1(), date, getPlayer1(), 900));

        //when
        var loaded = adapter.loadEntries(savedGames, date.plusDays(1));

        //then
        assertThat(loaded).isEmpty();
    }

    @Test
    void shouldSaveAndLoadEntriesOnRequestedDateForManyGames() {

        //given
        var entry1 = new DailyEntry(null, getGame1(), date, getPlayer1(), 900);
        var entry2 = new DailyEntry(null, getGame2(), date, getPlayer1(), 860);
        var entry3 = new DailyEntry(null, getGame1(), date, getPlayer2(), 890);
        var entry4 = new DailyEntry(null, getGame2(), date, getPlayer2(), 930);

        //when
        //TODO [GEOG-11] refactor this code to use saveAll after save endpoint expanded to batch mode
        var saved1 = adapter.save(entry1);
        var saved2 = adapter.save(entry2);
        var saved3 = adapter.save(entry3);
        var saved4 = adapter.save(entry4);
        var loaded = adapter.loadEntries(savedGames, date);

        //then
        assertIdIsNotNull(saved1);
        assertEqualsIgnoringId(saved1, entry1);
        assertIdIsNotNull(saved2);
        assertEqualsIgnoringId(saved2, entry2);
        assertIdIsNotNull(saved3);
        assertEqualsIgnoringId(saved3, entry3);
        assertIdIsNotNull(saved4);
        assertEqualsIgnoringId(saved4, entry4);
        assertThat(loaded).contains(saved1, saved2, saved3, saved4);
    }

    @Test
    void shouldFailWhenSavingTheSameEntryTwice() {
        // given
        var entry = new DailyEntry(null, getGame1(), date, getPlayer1(), 900);
        adapter.save(entry);

        // when / then
        assertThatThrownBy(() -> adapter.save(entry))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownGameId() {
        // given
        var unknownGame = new Game(new GameId(999L), "Unknown", ScoringSystem.STANDARD);
        var entry = new DailyEntry(null, unknownGame, date, getPlayer1(), 900);

        // when / then
        assertThatThrownBy(() -> adapter.save(entry))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownPlayerId() {
        // given
        var unknownPlayer = new Player(new PlayerId(999L), "Unknown");
        var entry = new DailyEntry(null, getGame1(), date, unknownPlayer, 900);

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

    private Game getGame1() {
        return savedGames.getFirst();
    }

    private Game getGame2() {
        return savedGames.get(1);
    }

    private Player getPlayer1() {
        return savedPlayers.getFirst();
    }

    private Player getPlayer2() {
        return savedPlayers.get(1);
    }
}
