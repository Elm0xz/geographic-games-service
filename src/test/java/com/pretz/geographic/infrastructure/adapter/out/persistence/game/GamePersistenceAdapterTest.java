package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.out.exception.GameNotFoundException;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({GamePersistenceAdapter.class, GamePersistenceMapper.class})
class GamePersistenceAdapterTest extends AbstractPostgresDataJpaTest {

    @Autowired
    private GamePersistenceAdapter adapter;

    @Autowired
    private GamePersistenceMapper mapper;

    @Autowired
    private GameJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Game game;

    @BeforeEach
    void setupExistingGame() {
        jdbcTemplate.execute("TRUNCATE TABLE game RESTART IDENTITY CASCADE");
        game = mapper.toDomain(repository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD)));
    }

    @Test
    void shouldLoadGameById() {
        //given
        var id = game.gameId().id();
        //when
        var result = adapter.loadGame(id);
        //then
        assertThat(result).isEqualTo(game);
    }

    @Test
    void shouldLoadAllActiveGames() {
        //given
        repository.deleteAllInBatch();
        var games = repository.saveAll(
                        List.of(new GameJpaEntity("Mapster", ScoringSystem.STANDARD),
                                new GameJpaEntity("WhenTaken", ScoringSystem.STANDARD),
                                new GameJpaEntity("Geogrid", ScoringSystem.STANDARD)))
                .stream().map(it -> mapper.toDomain(it)).toList();
        //when
        var result = adapter.loadActiveGames();
        //then
        assertThat(result).isEqualTo(games);
    }

    @Test
    void shouldFailWhenTryingToLoadGameWithUnknownId() {
        //given
        var id = 999L;
        //when / then
        assertThatThrownBy(() -> adapter.loadGame(id))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found for id: " + id);
    }
}
