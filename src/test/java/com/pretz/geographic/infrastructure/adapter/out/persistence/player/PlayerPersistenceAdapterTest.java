package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.exception.PlayerNotFoundException;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({PlayerPersistenceAdapter.class, PlayerPersistenceMapper.class})
class PlayerPersistenceAdapterTest extends AbstractPostgresDataJpaTest {

    @Autowired
    private PlayerPersistenceAdapter adapter;

    @Autowired
    private PlayerPersistenceMapper mapper;

    @Autowired
    private PlayerJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Player player;

    @BeforeEach
    void setupExistingPlayer() {
        jdbcTemplate.execute("TRUNCATE TABLE player RESTART IDENTITY CASCADE");
        player = mapper.toDomain(repository.save(new PlayerJpaEntity("Andrzej")));
    }

    @Test
    void shouldLoadPlayerById() {
        //given
        var id = player.playerId().id();
        //when
        var result = adapter.loadPlayer(id);
        //then
        assertThat(result).isEqualTo(player);
    }

    @Test
    void shouldLoadPlayerByName() {
        //given
        var name = player.name();
        //when
        var result = adapter.loadPlayer(name);
        //then
        assertThat(result).isEqualTo(player);
    }

    @Test
    void shouldFailWhenTryingToLoadPlayerWithUnknownId() {
        //given
        var id = 999L;
        //when / then
        assertThatThrownBy(() -> adapter.loadPlayer(id))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found for id: " + id);
    }

    @Test
    void shouldFailWhenTryingToLoadPlayerWithUnknownName() {
        //given
        var name = "Marcjan";
        //when / then
        assertThatThrownBy(() -> adapter.loadPlayer(name))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found for name: " + name);
    }
}
