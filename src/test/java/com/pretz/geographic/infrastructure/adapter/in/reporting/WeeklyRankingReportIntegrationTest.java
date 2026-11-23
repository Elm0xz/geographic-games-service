package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking.WeeklyPositionJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking.WeeklyRankingJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking.WeeklyRankingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(roles = "USER_READ")
public class WeeklyRankingReportIntegrationTest extends AbstractWebIntegrationTest {

    private static final String ENDPOINT = "/api/reports/weekly-rankings";
    private static final String GAME_ENDPOINT = ENDPOINT + "/games/{gameId}";

    private static final String YEAR = "2025";
    private static final String FROM_WEEK = "1";
    private static final String TO_WEEK = "3";
    private static final int SEEDED_RANKINGS = 4;

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private DailyEntryJpaRepository dailyEntryJpaRepository;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private WeeklyRankingJpaRepository weeklyRankingJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE weekly_ranking, daily_entry, game, player RESTART IDENTITY CASCADE");
        var mapster = gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));
        var whenTaken = gameJpaRepository.save(new GameJpaEntity("WhenTaken", ScoringSystem.STANDARD));
        var andrzej = playerJpaRepository.save(new PlayerJpaEntity("Andrzej"));
        var ferdynand = playerJpaRepository.save(new PlayerJpaEntity("Ferdynand"));
        persistRanking(mapster, 2024, 52, new PositionSeed(andrzej, 1, 900));
        persistRanking(mapster, 2025, 2, new PositionSeed(andrzej, 2, 1820), new PositionSeed(ferdynand, 0, 1720));
        persistRanking(whenTaken, 2025, 2, new PositionSeed(andrzej, 1, 700));
        // outside of requested interval
        persistRanking(mapster, 2025, 5, new PositionSeed(ferdynand, 1, 999));
        // daily entries of a week without persisted ranking must not be calculated by reporting
        var notCalculatedDate = LocalDate.of(2025, 1, 13);
        dailyEntryJpaRepository.save(new DailyEntryJpaEntity(whenTaken, ferdynand, notCalculatedDate, 950,
                notCalculatedDate.atStartOfDay().toInstant(UTC)));
    }

    @Test
    void shouldReturnWeeklyRankingsOfAllGamesForIntervalSpanningYearBoundary() {

        assertThat(mockMvc.get().uri(ENDPOINT)
                .param("fromYear", "2024").param("fromWeek", "52")
                .param("toYear", YEAR).param("toWeek", TO_WEEK))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "from": { "year": 2024, "week": 52 },
                          "to": { "year": 2025, "week": 3 },
                          "weeklyRankings": [
                            {
                              "game": { "id": 1, "name": "Mapster" },
                              "week": { "year": 2024, "week": 52 },
                              "playerResults": [
                                { "player": { "id": 1, "name": "Andrzej" }, "wins": 1, "points": 900.0 }
                              ]
                            },
                            {
                              "game": { "id": 1, "name": "Mapster" },
                              "week": { "year": 2025, "week": 2 },
                              "playerResults": [
                                { "player": { "id": 1, "name": "Andrzej" },   "wins": 2, "points": 1820.0 },
                                { "player": { "id": 2, "name": "Ferdynand" }, "wins": 0, "points": 1720.0 }
                              ]
                            },
                            {
                              "game": { "id": 2, "name": "WhenTaken" },
                              "week": { "year": 2025, "week": 2 },
                              "playerResults": [
                                { "player": { "id": 1, "name": "Andrzej" }, "wins": 1, "points": 700.0 }
                              ]
                            }
                          ]
                        }
                        """);
        assertThat(persistedRankingsCount()).isEqualTo(SEEDED_RANKINGS);
    }

    @Test
    void shouldReturnWeeklyRankingsOfSingleGameForWeekInterval() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 2)
                .param("fromYear", YEAR).param("fromWeek", FROM_WEEK)
                .param("toYear", YEAR).param("toWeek", TO_WEEK))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        {
                          "from": { "year": 2025, "week": 1 },
                          "to": { "year": 2025, "week": 3 },
                          "weeklyRankings": [
                            {
                              "game": { "id": 2, "name": "WhenTaken" },
                              "week": { "year": 2025, "week": 2 },
                              "playerResults": [
                                { "player": { "id": 1, "name": "Andrzej" }, "wins": 1, "points": 700.0 }
                              ]
                            }
                          ]
                        }
                        """);
        assertThat(persistedRankingsCount()).isEqualTo(SEEDED_RANKINGS);
    }

    @Test
    void shouldNotCalculateRankingOfWeekWithDailyEntriesButWithoutPersistedRanking() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 2)
                .param("fromYear", YEAR).param("fromWeek", TO_WEEK)
                .param("toYear", YEAR).param("toWeek", TO_WEEK))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        { "from": { "year": 2025, "week": 3 }, "to": { "year": 2025, "week": 3 }, "weeklyRankings": [] }
                        """);
        assertThat(persistedRankingsCount()).isEqualTo(SEEDED_RANKINGS);
    }

    @Test
    void shouldReturnNotFoundWhenGameDoesNotExist() {

        assertThat(mockMvc.get().uri(GAME_ENDPOINT, 99)
                .param("fromYear", YEAR).param("fromWeek", FROM_WEEK)
                .param("toYear", YEAR).param("toWeek", TO_WEEK))
                .hasFailed()
                .hasStatus(404)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("NOT_FOUND"));
    }

    @ParameterizedTest
    @MethodSource("invalidIntervals")
    void shouldReturnBadRequestOnInvalidInterval(String fromYear, String fromWeek, String toYear, String toWeek) {

        for (var request : List.of(mockMvc.get().uri(ENDPOINT), mockMvc.get().uri(GAME_ENDPOINT, 1))) {
            assertThat(request
                    .param("fromYear", fromYear).param("fromWeek", fromWeek)
                    .param("toYear", toYear).param("toWeek", toWeek))
                    .hasFailed()
                    .hasStatus(400)
                    .bodyJson()
                    .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_DATE"));
        }
        assertThat(persistedRankingsCount()).isEqualTo(SEEDED_RANKINGS);
    }

    @ParameterizedTest
    @MethodSource("malformedRequests")
    void shouldReturnBadRequestForMalformedRequest(String uri, String fromYear, String fromWeek, String toYear, String toWeek) {

        MockMvcRequestBuilder request = mockMvc.get().uri(uri);
        if (fromYear != null) request = request.param("fromYear", fromYear);
        if (fromWeek != null) request = request.param("fromWeek", fromWeek);
        if (toYear != null) request = request.param("toYear", toYear);
        if (toWeek != null) request = request.param("toWeek", toWeek);

        assertThat(request)
                .hasFailed()
                .hasStatus(400)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("MALFORMED_REQUEST"));
    }

    public static Stream<Arguments> invalidIntervals() {
        LocalDate now = LocalDate.now();
        String currentYear = String.valueOf(now.getYear());
        String currentWeek = String.valueOf(now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        return Stream.of(
                Arguments.of(YEAR, TO_WEEK, YEAR, FROM_WEEK),
                Arguments.of("2026", "1", YEAR, TO_WEEK),
                Arguments.of(YEAR, FROM_WEEK, currentYear, currentWeek),
                Arguments.of(YEAR, FROM_WEEK, String.valueOf(now.getYear() + 1), "1"),
                Arguments.of(YEAR, "54", YEAR, TO_WEEK),
                Arguments.of(YEAR, FROM_WEEK, YEAR, "0"));
    }

    public static Stream<Arguments> malformedRequests() {
        return Stream.of(
                Arguments.of(ENDPOINT, "not-a-year", FROM_WEEK, YEAR, TO_WEEK),
                Arguments.of(ENDPOINT, YEAR, "not-a-week", YEAR, TO_WEEK),
                Arguments.of(ENDPOINT, YEAR, FROM_WEEK, null, TO_WEEK),
                Arguments.of(ENDPOINT, YEAR, FROM_WEEK, YEAR, null),
                Arguments.of(ENDPOINT + "/games/not-a-number", YEAR, FROM_WEEK, YEAR, TO_WEEK));
    }

    private Integer persistedRankingsCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM weekly_ranking", Integer.class);
    }

    private void persistRanking(GameJpaEntity game, int year, int week, PositionSeed... positions) {
        var ranking = new WeeklyRankingJpaEntity(game, year, week, new ArrayList<>());
        ranking.setPositions(Arrays.stream(positions)
                .map(it -> new WeeklyPositionJpaEntity(ranking, it.player(), it.wins(), it.points()))
                .toList());
        weeklyRankingJpaRepository.save(ranking);
    }

    private record PositionSeed(PlayerJpaEntity player, int wins, int points) {
    }
}
