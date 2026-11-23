package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.validation.GameNotFoundException;
import com.pretz.geographic.application.port.in.CreateGameCommand;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.SaveGamePort;
import com.pretz.geographic.application.port.out.exception.GameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private LoadGamePort loadGamePort;

    @Mock
    private SaveGamePort saveGamePort;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(loadGamePort, saveGamePort);
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

    @Test
    void shouldReturnGameById() {
        //given
        var existing = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        when(loadGamePort.loadGames(List.of(1L))).thenReturn(List.of(existing));
        //when
        var result = gameService.getGame(new GameId(1L));
        //then
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void shouldThrowNotFoundWhenGameDoesNotExist() {
        //given
        when(loadGamePort.loadGames(List.of(99L))).thenReturn(List.of());
        //when, then
        assertThatThrownBy(() -> gameService.getGame(new GameId(99L)))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void shouldCreateGameWithoutId() {
        //given
        var saved = new Game(new GameId(4L), "Geogrid", ScoringSystem.STANDARD);
        when(saveGamePort.save(new Game(null, "Geogrid", ScoringSystem.STANDARD))).thenReturn(saved);
        //when
        var result = gameService.createGame(new CreateGameCommand("Geogrid", ScoringSystem.STANDARD));
        //then
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void shouldPropagateAlreadyExistsWhenGameNameIsTaken() {
        //given
        when(saveGamePort.save(any())).thenThrow(new GameAlreadyExistsException("Geogrid", null));
        //when, then
        assertThatThrownBy(() -> gameService.createGame(new CreateGameCommand("Geogrid", ScoringSystem.STANDARD)))
                .isInstanceOf(GameAlreadyExistsException.class);
    }

    @Test
    void shouldCreateGameWithStandardScoringSystemWhenNoneGiven() {
        //given
        var saved = new Game(new GameId(4L), "Geogrid", ScoringSystem.STANDARD);
        when(saveGamePort.save(new Game(null, "Geogrid", ScoringSystem.STANDARD))).thenReturn(saved);
        //when
        var result = gameService.createGame(new CreateGameCommand("Geogrid", null));
        //then
        assertThat(result).isEqualTo(saved);
    }
}
