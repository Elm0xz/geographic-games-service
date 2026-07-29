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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
//@DataJpaTest
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

    private final Game game = new Game(null, "Mapster", ScoringSystem.STANDARD);
    private final Player player = new Player(null, "Player1");
    private final LocalDate date = LocalDate.of(2026, 6, 30);
    private Game savedGame;
    private Player savedPlayer;

    @BeforeEach
    void initEntities() {
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

    /*
    TODO happy path
        TODO Should create daily entries
    TODO validations - syntax
        TODO Should return bad request for structurally invalid request
        TODO missing game
        TODO missing player
        TODO missing date
        TODO malformed IDs
        TODO batch failures?
    TODO validations - business
        TODO Should return bad request for semantic/business validation failure
        TODO future date
        TODO game name mismatch
        TODO player name mismatch
        TODO game/player not found if applicable
        TODO batch failures?
     */
}
