package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.validation.PlayerNotFoundException;
import com.pretz.geographic.application.port.in.CreatePlayerCommand;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SavePlayerPort;
import com.pretz.geographic.application.port.out.exception.PlayerAlreadyExistsException;
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
class PlayerServiceTest {

    @Mock
    private LoadPlayerPort loadPlayerPort;

    @Mock
    private SavePlayerPort savePlayerPort;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(loadPlayerPort, savePlayerPort);
    }

    @Test
    void shouldReturnAllPlayersSortedByName() {
        //given
        var ferdynand = new Player(new PlayerId(1L), "Ferdynand");
        var andrzej = new Player(new PlayerId(2L), "Andrzej");
        var bogumila = new Player(new PlayerId(3L), "Bogumiła");
        when(loadPlayerPort.loadAllPlayers()).thenReturn(List.of(ferdynand, andrzej, bogumila));
        //when
        var result = playerService.getAllPlayers();
        //then
        assertThat(result).containsExactly(andrzej, bogumila, ferdynand);
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersExist() {
        //given
        when(loadPlayerPort.loadAllPlayers()).thenReturn(List.of());
        //when
        var result = playerService.getAllPlayers();
        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnPlayerById() {
        //given
        var existing = new Player(new PlayerId(1L), "Andrzej");
        when(loadPlayerPort.loadPlayers(List.of(1L))).thenReturn(List.of(existing));
        //when
        var result = playerService.getPlayer(new PlayerId(1L));
        //then
        assertThat(result).isEqualTo(existing);
    }

    @Test
    void shouldThrowNotFoundWhenPlayerDoesNotExist() {
        //given
        when(loadPlayerPort.loadPlayers(List.of(99L))).thenReturn(List.of());
        //when, then
        assertThatThrownBy(() -> playerService.getPlayer(new PlayerId(99L)))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void shouldCreatePlayerWithoutId() {
        //given
        var saved = new Player(new PlayerId(4L), "Łucja");
        when(savePlayerPort.save(new Player(null, "Łucja"))).thenReturn(saved);
        //when
        var result = playerService.createPlayer(new CreatePlayerCommand("Łucja"));
        //then
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void shouldPropagateAlreadyExistsWhenPlayerNameIsTaken() {
        //given
        when(savePlayerPort.save(any())).thenThrow(new PlayerAlreadyExistsException("Łucja", null));
        //when, then
        assertThatThrownBy(() -> playerService.createPlayer(new CreatePlayerCommand("Łucja")))
                .isInstanceOf(PlayerAlreadyExistsException.class);
    }
}
