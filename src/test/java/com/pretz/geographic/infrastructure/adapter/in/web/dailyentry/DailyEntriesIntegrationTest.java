package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking.WeeklyRankingJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking.WeeklyRankingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyEntriesIntegrationTest extends AbstractWebIntegrationTest {

    private static final String ENDPOINT = "/api/daily-entries";
    private static final String BATCH_ENDPOINT = "/api/daily-entries/batch";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private DailyEntryJpaRepository dailyEntryJpaRepository;

    @Autowired
    private WeeklyRankingJpaRepository weeklyRankingJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Game game = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player = new Player(null, "Player1");
    private final Player player2 = new Player(null, "Player2");

    private GameJpaEntity savedGame;
    private PlayerJpaEntity savedPlayer;

    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE daily_entry, game, player RESTART IDENTITY CASCADE");
        savedGame = gameJpaRepository.save(new GameJpaEntity(game.name(), game.scoringSystem()));
        savedPlayer = playerJpaRepository.save(new PlayerJpaEntity(player.name()));
        playerJpaRepository.save(new PlayerJpaEntity(player2.name()));
    }

    @Test
    void shouldReturnCreatedForValidCreateDailyEntryRequest() throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "game": {
                                    "id": 1,
                                    "name": "Mapster"
                                  },
                                  "player": {
                                    "id": 1,
                                    "name": "Player1"
                                  },
                                  "date": "2026-07-29",
                                  "points": 990,
                                  "submittedAt": "2026-07-29T00:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("structurallyInvalidRequests")
    void shouldReturnBadRequestForStructurallyInvalidRequest(String invalidRequest, String errorCode) throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value(errorCode));
    }

    @ParameterizedTest
    @MethodSource("semanticallyIncorrectRequests")
    void shouldReturnBadRequestForSemanticAndBusinessValidationFailure(String incorrectRequest, String errorCode) throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incorrectRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value(errorCode));
    }

    @Test
    void shouldReturnCreatedWithNewEntriesForValidBatchRequest() throws Exception {

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T10:00:00Z"),
                                entryJson(1, "Mapster", 2, "Player2", "2026-07-29", 950, "2026-07-29T11:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(2)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[*].player.name", contains("Player1", "Player2")))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[*].points", contains(990, 950)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[*].successCode", contains("NEW", "NEW")))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(0)));

        assertThat(dailyEntryJpaRepository.findAll())
                .extracting(DailyEntryJpaEntity::getPoints)
                .containsExactlyInAnyOrder(990, 950);
    }

    @Test
    void shouldSaveOnlyEarliestOfDuplicatedEntriesInBatch() throws Exception {

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 900, "2026-07-29T18:00:00Z"),
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T08:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].points").value(990))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].successCode").value("NEW"))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[0].failureCode", contains("SUPERSEDED")));

        var entries = dailyEntryJpaRepository.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getPoints()).isEqualTo(990);
        assertThat(entries.getFirst().getSubmittedAt()).isEqualTo(Instant.parse("2026-07-29T08:00:00Z"));
    }

    @Test
    void shouldCorrectPersistedEntryWhenBatchEntryWasSubmittedEarlier() throws Exception {

        var date = LocalDate.of(2026, 7, 29);
        var persisted = dailyEntryJpaRepository.save(
                new DailyEntryJpaEntity(savedGame, savedPlayer, date, 800, Instant.parse("2026-07-29T12:00:00Z")));

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T08:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].id").value(persisted.getId()))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].points").value(990))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].successCode").value("CORRECTED"))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(0)));

        var entries = dailyEntryJpaRepository.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getId()).isEqualTo(persisted.getId());
        assertThat(entries.getFirst().getPoints()).isEqualTo(990);
        assertThat(entries.getFirst().getSubmittedAt()).isEqualTo(Instant.parse("2026-07-29T08:00:00Z"));
    }

    @Test
    void shouldSupersedeBatchEntryWhenPersistedEntryWasSubmittedEarlier() throws Exception {

        var date = LocalDate.of(2026, 7, 29);
        dailyEntryJpaRepository.save(
                new DailyEntryJpaEntity(savedGame, savedPlayer, date, 800, Instant.parse("2026-07-29T08:00:00Z")));

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T12:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(0)))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[0].failureCode", contains("SUPERSEDED")));

        var entries = dailyEntryJpaRepository.findAll();
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getPoints()).isEqualTo(800);
    }

    @Test
    void shouldSaveValidEntriesAndReturnFailuresForInvalidOnesInBatch() throws Exception {

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T10:00:00Z"),
                                entryJson(999, "Unknown", 1, "Player1", "2026-07-29", 950, "2026-07-29T10:00:00Z"),
                                entryJson(1, "Mapster", 999, "Unknown", "2026-07-29", 940, "2026-07-29T10:00:00Z"),
                                entryJson(1, "Mapster", 2, "Player2", "2026-07-29", 930, "2026-07-28T10:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].points").value(990))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(3)))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[0].failureCode", contains("UNKNOWN_GAME")))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[1].failureCode", contains("UNKNOWN_PLAYER")))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[2].failureCode", contains("INVALID_DATE")));

        assertThat(dailyEntryJpaRepository.findAll())
                .extracting(DailyEntryJpaEntity::getPoints)
                .containsExactly(990);
    }

    @Test
    void shouldReturnWeekClosedFailureForEntryInAlreadyCalculatedWeek() throws Exception {

        var closedWeekDate = LocalDate.of(2026, 7, 29);
        weeklyRankingJpaRepository.save(new WeeklyRankingJpaEntity(savedGame,
                closedWeekDate.get(IsoFields.WEEK_BASED_YEAR),
                closedWeekDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                List.of()));

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson(
                                entryJson(1, "Mapster", 1, "Player1", "2026-07-29", 990, "2026-07-29T10:00:00Z"),
                                entryJson(1, "Mapster", 1, "Player1", "2026-08-05", 950, "2026-08-05T10:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntrySuccessResponseDtoList[0].date").value("2026-08-05"))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList", hasSize(1)))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[0].date").value("2026-07-29"))
                .andExpect(jsonPath("$.createDailyEntryFailureResponseDtoList[0].failureCode", contains("WEEK_CLOSED")));

        assertThat(dailyEntryJpaRepository.findAll())
                .extracting(DailyEntryJpaEntity::getEntryDate)
                .containsExactly(LocalDate.of(2026, 8, 5));
    }

    @Test
    void shouldReturnBadRequestForEmptyBatchRequest() throws Exception {

        mockMvc.perform(post(BATCH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static String batchJson(String... entries) {
        return """
                {
                  "createDailyEntryRequestDtos": [%s]
                }
                """.formatted(String.join(",", entries));
    }

    private static String entryJson(long gameId, String gameName, long playerId, String playerName,
                                    String date, int points, String submittedAt) {
        return """
                {
                  "game": {
                    "id": %d,
                    "name": "%s"
                  },
                  "player": {
                    "id": %d,
                    "name": "%s"
                  },
                  "date": "%s",
                  "points": %d,
                  "submittedAt": "%s"
                }
                """.formatted(gameId, gameName, playerId, playerName, date, points, submittedAt);
    }

    private static Stream<Arguments> structurallyInvalidRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "VALIDATION_ERROR"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "VALIDATION_ERROR"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "VALIDATION_ERROR"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990
                        }
                        """, "VALIDATION_ERROR"),
                Arguments.of("""
                        {
                          "game": {
                            "id": "not-a-number",
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "MALFORMED_REQUEST"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": "not-a-number",
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "MALFORMED_REQUEST"),
                Arguments.of("""
                        {
                          "game": {
                            "id": -1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "VALIDATION_ERROR"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": -1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "VALIDATION_ERROR")
        );
    }

    private static Stream<Arguments> semanticallyIncorrectRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Wrong Name"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "INVALID_GAME_NAME"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Wrong Name"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "INVALID_PLAYER_NAME"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 999,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 1,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "GAME_NOT_FOUND"),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "id": 999,
                            "name": "Player1"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "PLAYER_NOT_FOUND")/*,
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "name": "Unknown"
                          },
                          "date": "2026-07-29",
                          "points": 990,
                          "submittedAt": "2026-07-29T00:00:00Z"
                        }
                        """, "PLAYER_NOT_FOUND")*/
        );
    }
}
