package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking;

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
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

//TODO [GEOG-16] create shared class for integration tests & separate package (?)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class WeeklyRankingIntegrationTest {

    private static final String ENDPOINT = "/api/weekly-ranking";

    // Week 2 of 2025: Mon 2025-01-06 to Sun 2025-01-12
    private static final String PAST_YEAR = "2025";
    private static final String PAST_WEEK = "2";

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

    private final Game game1 = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player1 = new Player(null, "Andrzej");
    private final Player player2 = new Player(null, "Ferdynand");

    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE weekly_ranking, daily_entry, game, player RESTART IDENTITY CASCADE");
        var savedGames = gameJpaRepository.saveAll(List.of(
                new GameJpaEntity(game1.name(), game1.scoringSystem())));
        var savedPlayers = playerJpaRepository.saveAll(List.of(
                new PlayerJpaEntity(player1.name()),
                new PlayerJpaEntity(player2.name())));
        dailyEntryJpaRepository.saveAll(List.of(
                new DailyEntryJpaEntity(savedGames.getFirst(), savedPlayers.getFirst(), LocalDate.of(2025, 1, 6), 900),
                new DailyEntryJpaEntity(savedGames.getFirst(), savedPlayers.get(1), LocalDate.of(2025, 1, 6), 850),
                new DailyEntryJpaEntity(savedGames.getFirst(), savedPlayers.getFirst(), LocalDate.of(2025, 1, 7), 920),
                new DailyEntryJpaEntity(savedGames.getFirst(), savedPlayers.get(1), LocalDate.of(2025, 1, 7), 870)
        ));
    }

    @Test
    void shouldReturnOkWithCorrectBodyForValidWeeklyRankingRequest() {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("year", PAST_YEAR)
                .param("week", PAST_WEEK))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        {
                          "year": 2025,
                          "week": 2,
                          "weeklyRankings": [
                            {
                              "game": { "gameId": 1, "name": "Mapster" },
                              "playerResults": [
                                { "player": { "playerId": 1, "name": "Andrzej" },   "wins": 2, "points": 1820.0 },
                                { "player": { "playerId": 2, "name": "Ferdynand" }, "wins": 0, "points": 1720.0 }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @ParameterizedTest
    @MethodSource("invalidWeeks")
    void shouldReturnBadRequestOnWeekNotInPast(String year, String week) {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("year", year)
                .param("week", week))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_DATE"));
    }

    @ParameterizedTest
    @MethodSource("malformedRequests")
    void shouldReturnBadRequestForMalformedParameters(String year, String week) {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("year", year)
                .param("week", week))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnBadRequestWhenYearParameterIsMissing() {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("week", PAST_WEEK))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnBadRequestWhenWeekParameterIsMissing() {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("year", PAST_YEAR))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    public static Stream<Arguments> invalidWeeks() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentWeek = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return Stream.of(
                Arguments.of(String.valueOf(currentYear + 1), "1"),
                Arguments.of(String.valueOf(currentYear), String.valueOf(currentWeek)),
                Arguments.of(String.valueOf(currentYear), String.valueOf(currentWeek + 1)),
                Arguments.of(String.valueOf(currentYear - 1), "54"),
                Arguments.of(String.valueOf(currentYear - 1), "-1")
        );
    }

    public static Stream<Arguments> malformedRequests() {
        return Stream.of(
                Arguments.of("not-a-year", "1"),
                Arguments.of("2025", "not-a-week")
        );
    }
}