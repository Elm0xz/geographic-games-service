package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.out.LoadGamePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private LoadGamePort loadGamePort;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(loadGamePort);
    }

    @Test
    void shouldReturnAllGamesSortedByName() {
        //given
        var whenTaken = new Game(new GameId(1L), "WhenTaken", ScoringSystem.STANDARD);
        var mapster = new Game(new GameId(2L), "Mapster", ScoringSystem.STANDARD);
        var geogrid = new Game(new GameId(3L), "Geogrid", ScoringSystem.STANDARD);
        when(loadGamePort.loadAllGames()).thenReturn(List.of(whenTaken, mapster, geogrid));
        //when
        var result = gameService.getAllGames();
        //then
        assertThat(result).containsExactly(geogrid, mapster, whenTaken);
    }

    @Test
    void shouldReturnEmptyListWhenNoGamesExist() {
        //given
        when(loadGamePort.loadAllGames()).thenReturn(List.of());
        //when
        var result = gameService.getAllGames();
        //then
        assertThat(result).isEmpty();
    }
}
