package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.out.exception.GameNotFoundException;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({GamePersistenceAdapter.class, GamePersistenceMapper.class})
class GamePersistenceAdapterTest extends AbstractPostgresDataJpaTest {

    @Autowired
    private GamePersistenceAdapter adapter;

    @Autowired
    private GamePersistenceMapper mapper;

    @Autowired
    private GameJpaRepository repository;

    private Game game;

    @BeforeEach
    void setupExistingGame() {
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
