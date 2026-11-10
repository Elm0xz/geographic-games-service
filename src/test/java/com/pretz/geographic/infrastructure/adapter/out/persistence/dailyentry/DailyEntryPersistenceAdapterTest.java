package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.out.exception.ConcurrentModificationConflictException;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GamePersistenceMapper;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Game game1 = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player1 = new Player(null, "Player1");
    private final Game game2 = new Game(null, "WhenTaken", ScoringSystem.STANDARD);
    private final Player player2 = new Player(null, "Player2");
    private final LocalDate date = LocalDate.of(2026, 6, 30);
    private final Instant noon = date.atTime(12, 0).toInstant(UTC);
    private List<Game> savedGames;
    private List<Player> savedPlayers;

    @BeforeEach
    void setupGamesAndPlayers() {
        jdbcTemplate.execute("TRUNCATE TABLE daily_entry, game, player RESTART IDENTITY CASCADE");
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
    }

    @Test
    void shouldSaveAndLoadEntriesOnRequestedDate() {

        //given
        var entry = entry(getGame1(), date, getPlayer1(), 900);

        //when
        var saved = adapter.saveAll(List.of(entry)).getFirst();
        var loaded = adapter.loadEntries(savedGames, date);

        //then
        assertIdIsNotNull(saved);
        assertEqualsIgnoringId(saved, entry);
        assertThat(loaded).containsExactly(saved);
    }

    @Test
    void shouldNotLoadEntriesOutsideDateRange() {

        //given
        adapter.saveAll(List.of(entry(getGame1(), date, getPlayer1(), 900)));

        //when
        var loaded = adapter.loadEntries(savedGames, date.plusDays(1));

        //then
        assertThat(loaded).isEmpty();
    }

    @Test
    void shouldSaveAndLoadEntriesOnRequestedDateForManyGames() {

        //given
        var entry1 = entry(getGame1(), date, getPlayer1(), 900);
        var entry2 = entry(getGame2(), date, getPlayer1(), 860);
        var entry3 = entry(getGame1(), date, getPlayer2(), 890);
        var entry4 = entry(getGame2(), date, getPlayer2(), 930);

        //when
        var saved = adapter.saveAll(List.of(entry1, entry2, entry3, entry4));
        var loaded = adapter.loadEntries(savedGames, date);

        //then the order of returned entries is not part of the contract
        assertThat(saved).hasSize(4);
        saved.forEach(this::assertIdIsNotNull);
        assertThat(saved)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("dailyEntryId")
                .containsExactlyInAnyOrder(entry1, entry2, entry3, entry4);
        assertThat(loaded).containsExactlyInAnyOrderElementsOf(saved);
    }

    @Test
    void shouldLoadEntriesInDateInterval() {

        //given
        var startDate = date.minusDays(2);
        var endDate = date.plusDays(2);
        var entryBeforeStart = entry(getGame1(), startDate.minusDays(1), getPlayer1(), 800);
        var entryOnStart = entry(getGame1(), startDate, getPlayer1(), 850);
        var entryInMiddle1 = entry(getGame1(), date, getPlayer1(), 900);
        var entryInMiddle2 = entry(getGame2(), date, getPlayer2(), 920);
        var entryOnEnd = entry(getGame1(), endDate, getPlayer1(), 880);
        var entryAfterEnd = entry(getGame1(), endDate.plusDays(1), getPlayer1(), 870);

        //when
        adapter.saveAll(List.of(entryBeforeStart, entryOnStart, entryInMiddle1, entryInMiddle2, entryOnEnd, entryAfterEnd));
        var loaded = adapter.loadEntries(savedGames, startDate, endDate);

        //then
        assertThat(loaded)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("dailyEntryId")
                .containsExactlyInAnyOrder(entryOnStart, entryInMiddle1, entryInMiddle2, entryOnEnd);
    }

    @Test
    void shouldApplyCorrectionSubmittedEarlierThanStoredEntry() {

        //given
        var persisted = adapter.saveAll(List.of(entry(getGame1(), date, getPlayer1(), 900, noon))).getFirst();
        var correction = new DailyEntry(persisted.dailyEntryId(), getGame1(), date, getPlayer1(), 950, noon.minusSeconds(4 * 3600));
        flushAndClear();

        //when
        var saved = adapter.saveAll(List.of(correction));
        flushAndClear();
        var loaded = adapter.loadEntries(savedGames, date);

        //then
        assertThat(saved).containsExactly(correction);
        assertThat(loaded).containsExactly(correction);
    }

    @ParameterizedTest(name = "correction submitted {0}s after the stored entry")
    @ValueSource(longs = {3600, 0})
    void shouldRefuseCorrectionSubmittedLaterThanOrEqualToStoredEntry(long secondsAfterStored) {

        //given
        var persisted = adapter.saveAll(List.of(entry(getGame1(), date, getPlayer1(), 900, noon))).getFirst();
        var correction = new DailyEntry(persisted.dailyEntryId(), getGame1(), date, getPlayer1(), 950,
                noon.plusSeconds(secondsAfterStored));
        flushAndClear();

        //when / then
        assertThatThrownBy(() -> adapter.saveAll(List.of(correction)))
                .isInstanceOf(ConcurrentModificationConflictException.class);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT points FROM daily_entry WHERE id = ?", Integer.class, persisted.dailyEntryId().id()))
                .isEqualTo(900);
    }

    @Test
    void shouldReturnAllWrittenEntriesForMixedInsertsAndCorrections() {

        //given
        var persisted = adapter.saveAll(List.of(entry(getGame1(), date, getPlayer1(), 900, noon))).getFirst();
        flushAndClear();
        var correction = new DailyEntry(persisted.dailyEntryId(), getGame1(), date, getPlayer1(), 950, noon.minusSeconds(3600));
        var newEntry1 = entry(getGame2(), date, getPlayer1(), 800, noon);
        var newEntry2 = entry(getGame1(), date, getPlayer2(), 700, noon);

        //when
        var saved = adapter.saveAll(List.of(newEntry1, correction, newEntry2));
        flushAndClear();
        var loaded = adapter.loadEntries(savedGames, date);

        //then
        assertThat(saved).hasSize(3);
        saved.forEach(this::assertIdIsNotNull);
        assertThat(saved).contains(correction);
        assertThat(saved)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("dailyEntryId")
                .containsExactlyInAnyOrder(correction, newEntry1, newEntry2);
        assertThat(loaded).containsExactlyInAnyOrderElementsOf(saved);
    }

    @Test
    void shouldFailWithConflictWhenSavingTheSameEntryTwice() {
        // given
        var entry = entry(getGame1(), date, getPlayer1(), 900);
        adapter.saveAll(List.of(entry));

        // when / then
        assertThatThrownBy(() -> adapter.saveAll(List.of(entry)))
                .isInstanceOf(ConcurrentModificationConflictException.class);
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownGameId() {
        // given
        var unknownGame = new Game(new GameId(999L), "Unknown", ScoringSystem.STANDARD);
        var entry = entry(unknownGame, date, getPlayer1(), 900);

        // when / then
        assertThatThrownBy(() -> adapter.saveAll(List.of(entry)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldFailWhenSavingEntryWithUnknownPlayerId() {
        // given
        var unknownPlayer = new Player(new PlayerId(999L), "Unknown");
        var entry = entry(getGame1(), date, unknownPlayer, 900);

        // when / then
        assertThatThrownBy(() -> adapter.saveAll(List.of(entry)))
                .isInstanceOf(DataAccessException.class);
    }

    // The guarded update bypasses the persistence context; clear it so loads read from the database.
    private void flushAndClear() {
        testEntityManager.flush();
        testEntityManager.clear();
    }

    private DailyEntry entry(Game game, LocalDate date, Player player, int points) {
        return entry(game, date, player, points, date.atStartOfDay().toInstant(UTC));
    }

    private DailyEntry entry(Game game, LocalDate date, Player player, int points, Instant submittedAt) {
        return new DailyEntry(null, game, date, player, points, submittedAt);
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
