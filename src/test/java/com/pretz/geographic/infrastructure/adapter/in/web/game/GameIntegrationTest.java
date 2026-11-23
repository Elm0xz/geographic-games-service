package com.pretz.geographic.infrastructure.adapter.in.web.game;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
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

    @Test
    void shouldReturnGameById() {
        //given
        gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));

        //when, then
        assertThat(mockMvc.get().uri(ENDPOINT + "/1"))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("""
                        { "id": 1, "name": "Mapster" }
                        """);
    }

    @Test
    void shouldReturnNotFoundWhenGameDoesNotExist() {

        assertThat(mockMvc.get().uri(ENDPOINT + "/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("NOT_FOUND");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Geogrid", "Łódź Quiz", "Geo-Guess 2", "O'Brien", "map_ster.v2", "Ä"})
    @WithMockUser(roles = "ADMIN")
    void shouldCreateGameWithValidName(String name) {

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
    void shouldReturnConflictWhenGameNameIsTaken() {
        //given
        gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));

        //when, then
        assertThat(create("Mapster"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("RESOURCE_ALREADY_EXISTS");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateGameWhoseNameDiffersOnlyInCase() {
        //given
        gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));

        //when, then
        assertThat(create("mapster"))
                .hasStatus(HttpStatus.CREATED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " Mapster", "Mapster ", "-Mapster", "Map$ter", "Map<b>ster", "Mapster!",
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidGameName(String name) {

        assertThat(create(name))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectGameWithUnknownScoringSystem() {

        assertThat(mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Geogrid", "scoringSystem": "UNKNOWN" }
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.code").isEqualTo("MALFORMED_REQUEST");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateGameWithStandardScoringSystemWhenNoneGiven() {

        assertThat(mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Geogrid" }
                        """))
                .hasStatus(HttpStatus.CREATED);
        assertThat(gameJpaRepository.findAll())
                .singleElement()
                .satisfies(it -> assertThat(it.getScoringSystem()).isEqualTo(ScoringSystem.STANDARD));
    }

    @Test
    @WithMockUser(roles = "USER_WRITE")
    void shouldForbidCreatingGameForNonAdmin() {

        assertThat(create("Geogrid"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithAnonymousUser
    void shouldRejectCreatingGameForAnonymousUser() {

        assertThat(create("Geogrid"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MockMvcTester.MockMvcRequestBuilder create(String name) {
        return mockMvc.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "%s", "scoringSystem": "STANDARD" }
                        """.formatted(name));
    }
}
