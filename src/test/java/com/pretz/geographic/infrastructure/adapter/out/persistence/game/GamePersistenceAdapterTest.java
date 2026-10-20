package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldLoadGamesByIds() {
        //given
        var otherGame = mapper.toDomain(repository.save(new GameJpaEntity("WhenTaken", ScoringSystem.STANDARD)));
        //when
        var result = adapter.loadGames(List.of(game.gameId().id(), otherGame.gameId().id()));
        //then
        assertThat(result).containsExactlyInAnyOrder(game, otherGame);
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
    void shouldSkipUnknownIdsWhenLoadingGamesByIds() {
        //given
        var unknownId = 999L;
        //when
        var result = adapter.loadGames(List.of(game.gameId().id(), unknownId));
        //then
        assertThat(result).containsExactly(game);
    }
}
