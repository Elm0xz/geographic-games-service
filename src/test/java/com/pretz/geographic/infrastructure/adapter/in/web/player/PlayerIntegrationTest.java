package com.pretz.geographic.infrastructure.adapter.in.web.player;

import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(roles = "USER_READ")
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
                          { "id": 2, "name": "Andrzej" },
                          { "id": 1, "name": "Ferdynand" }
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

    @Test
    void shouldReturnPlayerById() {
        //given
        playerJpaRepository.save(new PlayerJpaEntity("Andrzej"));

        //when, then
        assertThat(mockMvc.get().uri(ENDPOINT + "/1"))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        { "id": 1, "name": "Andrzej" }
                        """);
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() {

        assertThat(mockMvc.get().uri(ENDPOINT + "/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("NOT_FOUND");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Łucja", "Jan Kowalski", "Anna-Maria 2", "O'Brien", "jan_kowalski.v2", "Ä"})
    @WithMockUser(roles = "ADMIN")
    void shouldCreatePlayerWithValidName(String name) {

        assertThat(create(name))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost" + ENDPOINT + "/1")
                .bodyJson()
                .isStrictlyEqualTo("""
                        { "id": 1, "name": "%s" }
                        """.formatted(name));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflictWhenPlayerNameIsTaken() {
        //given
        playerJpaRepository.save(new PlayerJpaEntity("Andrzej"));

        //when, then
        assertThat(create("Andrzej"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("RESOURCE_ALREADY_EXISTS");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreatePlayerWhoseNameDiffersOnlyInCase() {
        //given
        playerJpaRepository.save(new PlayerJpaEntity("Andrzej"));

        //when, then
        assertThat(create("andrzej"))
                .hasStatus(HttpStatus.CREATED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " Andrzej", "Andrzej ", "-Andrzej", "Andrz$ej", "Andrz<b>ej", "Andrzej!",
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidPlayerName(String name) {

        assertThat(create(name))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @WithMockUser(roles = "USER_WRITE")
    void shouldForbidCreatingPlayerForNonAdmin() {

        assertThat(create("Łucja"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    void shouldRejectCreatingPlayerForAnonymousUser() {

        assertThat(create("Łucja"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MockMvcTester.MockMvcRequestBuilder create(String name) {
        return mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "%s" }
                        """.formatted(name));
    }
}
