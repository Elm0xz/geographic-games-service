package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private LoadPlayerPort loadPlayerPort;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(loadPlayerPort);
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
}
