package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class DailyRankingIntegrationTest {

    private static final String ENDPOINT = "/api/daily-ranking";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private DailyEntryJpaRepository dailyEntryJpaRepository;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Game game = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player1 = new Player(null, "Andrzej");
    private final Player player2 = new Player(null, "Ferdynand");
    private final DailyEntry dailyEntry1 = new DailyEntry(null, game, LocalDate.of(2026, 4, 4), player1, 836);
    private final DailyEntry dailyEntry2 = new DailyEntry(null, game, LocalDate.of(2026, 4, 4), player2, 911);

    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE daily_entry, game, player RESTART IDENTITY CASCADE");
        var savedGame = gameJpaRepository.save(new GameJpaEntity(game.name(), game.scoringSystem()));
        var savedPlayers = playerJpaRepository.saveAll(List.of(new PlayerJpaEntity(player1.name()), new PlayerJpaEntity(player2.name())));
        dailyEntryJpaRepository.saveAll(List.of(
                new DailyEntryJpaEntity(savedGame, savedPlayers.getFirst(), dailyEntry1.date(), dailyEntry1.points()),
                new DailyEntryJpaEntity(savedGame, savedPlayers.get(1), dailyEntry2.date(), dailyEntry2.points())
        ));
    }

    @Test
    void shouldReturnOkForValidGetDailyRankingRequest() throws Exception {

        assertThat(mockMvc.get().uri(ENDPOINT).param("date", "2026-04-04"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        [
                          {
                            "game": { "gameId": 1, "name": "Mapster" },
                            "date": "2026-04-04",
                            "entries": [
                              { "id": 2, "player": "Ferdynand", "points": 911 },
                              { "id": 1, "player": "Andrzej",   "points": 836 }
                            ]
                          }
                        ]
                        """);
    }

    @ParameterizedTest
    @MethodSource("incorrectDates")
    void shouldReturnBadRequestOnDateNotInPast(String incorrectDate) {

        assertThat(mockMvc.get().uri(ENDPOINT).param("date", incorrectDate))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_DATE"));
    }

    @ParameterizedTest
    @MethodSource("syntacticallyInvalidRequests")
    void shouldReturnBadRequestForSyntacticallyInvalidRequest(String date) {

        assertThat(mockMvc.get().uri(ENDPOINT).param("date", date))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnBadRequestWhenDateParameterIsMissing() {

        assertThat(mockMvc.get().uri(ENDPOINT))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    public static Stream<Arguments> incorrectDates() {
        return Stream.of(Arguments.of(LocalDate.now().toString()), Arguments.of(LocalDate.now().plusDays(4).toString()));
    }

    public static Stream<Arguments> syntacticallyInvalidRequests() {
        return Stream.of(
                Arguments.of("not-a-date"),
                Arguments.of("29-07-2026"),
                Arguments.of("20260729")
        );
    }
}
