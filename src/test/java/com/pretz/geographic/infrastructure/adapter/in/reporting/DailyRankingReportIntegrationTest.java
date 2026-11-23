package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(roles = "USER_READ")
public class DailyRankingReportIntegrationTest extends AbstractWebIntegrationTest {

    private static final Instant SUBMITTED_AT = Instant.parse("2026-04-08T10:00:00Z");
    private static final String ENDPOINT = "/api/reports/daily-rankings";
    private static final String GAME_ENDPOINT = ENDPOINT + "/games/{gameId}";
    private static final String FROM = "2026-04-04";
    private static final String TO = "2026-04-06";

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

    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE weekly_ranking, daily_entry, game, player RESTART IDENTITY CASCADE");
        var mapster = gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));
        var whenTaken = gameJpaRepository.save(new GameJpaEntity("WhenTaken", ScoringSystem.STANDARD));
        var andrzej = playerJpaRepository.save(new PlayerJpaEntity("Andrzej"));
        var ferdynand = playerJpaRepository.save(new PlayerJpaEntity("Ferdynand"));
        dailyEntryJpaRepository.saveAll(List.of(
                new DailyEntryJpaEntity(mapster, andrzej, LocalDate.of(2026, 4, 4), 836, SUBMITTED_AT),
                new DailyEntryJpaEntity(mapster, ferdynand, LocalDate.of(2026, 4, 4), 911, SUBMITTED_AT),
                new DailyEntryJpaEntity(mapster, andrzej, LocalDate.of(2026, 4, 5), 900, SUBMITTED_AT),
                new DailyEntryJpaEntity(whenTaken, andrzej, LocalDate.of(2026, 4, 4), 777, SUBMITTED_AT),
                // outside of requested interval
                new DailyEntryJpaEntity(mapster, andrzej, LocalDate.of(2026, 4, 3), 500, SUBMITTED_AT),
                new DailyEntryJpaEntity(whenTaken, ferdynand, LocalDate.of(2026, 4, 7), 600, SUBMITTED_AT)
        ));
    }

    @Test
    void shouldReturnDailyRankingsOfAllGamesForDateInterval() {

        assertThat(mockMvc.get().uri(ENDPOINT).param("from", FROM).param("to", TO))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "from": "2026-04-04",
                          "to": "2026-04-06",
                          "dailyRankings": [
                            {
                              "game": { "id": 1, "name": "Mapster" },
                              "date": "2026-04-04",
                              "entries": [
                                { "id": 2, "player": { "id": 2, "name": "Ferdynand" }, "points": 911 },
                                { "id": 1, "player": { "id": 1, "name": "Andrzej" },   "points": 836 }
                              ]
                            },
                            {
                              "game": { "id": 1, "name": "Mapster" },
                              "date": "2026-04-05",
                              "entries": [
                                { "id": 3, "player": { "id": 1, "name": "Andrzej" }, "points": 900 }
                              ]
                            },
                            {
                              "game": { "id": 2, "name": "WhenTaken" },
                              "date": "2026-04-04",
                              "entries": [
                                { "id": 4, "player": { "id": 1, "name": "Andrzej" }, "points": 777 }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void shouldReturnDailyRankingsOfSingleGameForDateInterval() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 2).param("from", FROM).param("to", TO))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "from": "2026-04-04",
                          "to": "2026-04-06",
                          "dailyRankings": [
                            {
                              "game": { "id": 2, "name": "WhenTaken" },
                              "date": "2026-04-04",
                              "entries": [
                                { "id": 4, "player": { "id": 1, "name": "Andrzej" }, "points": 777 }
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void shouldReturnEmptyRankingsWhenNoEntriesExistInDateInterval() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 1).param("from", "2026-03-01").param("to", "2026-03-31"))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        { "from": "2026-03-01", "to": "2026-03-31", "dailyRankings": [] }
                        """);
    }

    @Test
    void shouldReturnNotFoundWhenGameDoesNotExist() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 99).param("from", FROM).param("to", TO))
                .hasFailed()
                .hasStatus(404)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("NOT_FOUND"));
    }

    @ParameterizedTest
    @MethodSource("invalidIntervals")
    void shouldReturnBadRequestOnInvalidInterval(String from, String to) {

        assertThat(mockMvc.get().uri(ENDPOINT).param("from", from).param("to", to))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_DATE"));
        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 1).param("from", from).param("to", to))
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_DATE"));
    }

    @ParameterizedTest
    @MethodSource("malformedRequests")
    void shouldReturnBadRequestForMalformedRequest(String uri, String from, String to) {

        var request = mockMvc.get().uri(uri);
        if (from != null) request = request.param("from", from);
        if (to != null) request = request.param("to", to);

        assertThat(request)
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    public static Stream<Arguments> invalidIntervals() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                Arguments.of(TO, FROM),
                Arguments.of(FROM, today.toString()),
                Arguments.of(today.toString(), today.plusDays(3).toString()));
    }

    public static Stream<Arguments> malformedRequests() {
        return Stream.of(
                Arguments.of(ENDPOINT, "not-a-date", TO),
                Arguments.of(ENDPOINT, FROM, "06-04-2026"),
                Arguments.of(ENDPOINT, null, TO),
                Arguments.of(ENDPOINT, FROM, null),
                Arguments.of(ENDPOINT + "/games/not-a-number", FROM, TO));
    }
}
