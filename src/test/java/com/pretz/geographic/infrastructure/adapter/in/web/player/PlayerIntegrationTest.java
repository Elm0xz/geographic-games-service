package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerIntegrationTest extends AbstractWebIntegrationTest {

    private static final String ENDPOINT = "/api/players";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void truncatePlayers() {
        jdbcTemplate.execute("TRUNCATE TABLE player RESTART IDENTITY CASCADE");
    }

    @Test
    void shouldReturnAllPlayersSortedByName() {
        //given
        playerJpaRepository.saveAll(List.of(
                new PlayerJpaEntity("Ferdynand"),
                new PlayerJpaEntity("Andrzej")));

        //when, then
        assertThat(mockMvc.get().uri(ENDPOINT))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        [
                          { "playerId": 2, "name": "Andrzej" },
                          { "playerId": 1, "name": "Ferdynand" }
                        ]
                        """);
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersExist() {

        assertThat(mockMvc.get().uri(ENDPOINT))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("[]");
    }
}
