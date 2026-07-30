package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.ScoringSystem;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class DailyEntriesIntegrationTest {

    private static final String ENDPOINT = "/api/daily-entries";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Game game = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player = new Player(null, "Player1");
    private final LocalDate date = LocalDate.of(2026, 6, 30);
    private Game savedGame;
    private Player savedPlayer;


    @BeforeEach
    void initEntities() {
        jdbcTemplate.execute("TRUNCATE TABLE daily_entry, game, player RESTART IDENTITY CASCADE");
        gameJpaRepository.save(new GameJpaEntity(game.name(), game.scoringSystem()));
        playerJpaRepository.save(new PlayerJpaEntity(player.name()));
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
                                  "points": 990
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("structurallyInvalidRequests")
    void shouldReturnBadRequestForStructurallyInvalidRequest(String invalidRequest) throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("semanticallyIncorrectRequests")
    void shouldReturnBadRequestForSemanticAndBusinessValidationFailure(String incorrectRequest) throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incorrectRequest))
                .andExpect(status().isBadRequest());
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
                          "points": 990
                        }
                        """),
                Arguments.of("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "date": "2026-07-29",
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """)
        );
    }

    private static Stream<Arguments> semanticallyIncorrectRequests() {
        return Stream.of(
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
                          "date": "2056-07-29",
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
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
                          "points": 990
                        }
                        """),
                Arguments.of(("""
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
                          "points": 990
                        }
                        """)),
                Arguments.of(("""
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
                          "points": 990
                        }
                        """)),
                Arguments.of(("""
                        {
                          "game": {
                            "id": 1,
                            "name": "Mapster"
                          },
                          "player": {
                            "name": "Unknown"
                          },
                          "date": "2026-07-29",
                          "points": 990
                        }
                        """)
                ));
    }
}
