package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GameIntegrationTest extends AbstractWebIntegrationTest {

    private static final String ENDPOINT = "/api/games";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void truncateGames() {
        jdbcTemplate.execute("TRUNCATE TABLE game RESTART IDENTITY CASCADE");
    }

    @Test
    void shouldReturnAllGamesSortedByName() {
        //given
        gameJpaRepository.saveAll(List.of(
                new GameJpaEntity("WhenTaken", ScoringSystem.STANDARD),
                new GameJpaEntity("Mapster", ScoringSystem.STANDARD),
                new GameJpaEntity("Geogrid", ScoringSystem.STANDARD)));

        //when, then
        assertThat(mockMvc.get().uri(ENDPOINT))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        [
                          { "id": 3, "name": "Geogrid" },
                          { "id": 2, "name": "Mapster" },
                          { "id": 1, "name": "WhenTaken" }
                        ]
                        """);
    }

    @Test
    void shouldReturnEmptyListWhenNoGamesExist() {

        assertThat(mockMvc.get().uri(ENDPOINT))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("[]");
    }
}
