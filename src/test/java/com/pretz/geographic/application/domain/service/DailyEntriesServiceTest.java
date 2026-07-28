package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.in.AddDailyEntryCommand;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyEntriesServiceTest {

    @Mock
    private SaveDailyEntryPort saveDailyEntryPort;

    @Mock
    private LoadGamePort loadGamePort;

    @Mock
    private LoadPlayerPort loadPlayerPort;

    private DailyEntriesService dailyEntriesService;

    @BeforeEach
    void setUp() {
        dailyEntriesService = new DailyEntriesService(
                saveDailyEntryPort,
                loadGamePort,
                loadPlayerPort,
                new GameNameValidator(),
                new PlayerNameValidator()
        );
    }

    @Test
    void shouldAddDailyEntryWhenGameAndPlayerNamesMatchPersistedOnes() {
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(
                1L,
                "Mapster",
                2L,
                "Player1",
                date,
                950
        );
        DailyEntry savedEntry = new DailyEntry(new DailyEntryId(10L), game, date, player, 950);

        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);
        when(saveDailyEntryPort.save(new DailyEntry(null, game, date, player, 950))).thenReturn(savedEntry);

        DailyEntry result = dailyEntriesService.addDailyEntry(command);

        assertThat(result).isEqualTo(savedEntry);

        verify(loadGamePort).loadGame(1L);
        verify(loadPlayerPort).loadPlayer(2L);
        verify(saveDailyEntryPort).save(new DailyEntry(null, game, date, player, 950));
    }

    @Test
    void shouldThrowInvalidGameNameExceptionWhenInputGameNameDoesNotMatchPersistedOne() {
        Game game = new Game(new GameId(2L), "Worldle", ScoringSystem.STANDARD);
        AddDailyEntryCommand command = command(
                2L,
                "WhenTaken",
                2L,
                "Player1",
                LocalDate.now().minusDays(10),
                950
        );
        when(loadGamePort.loadGame(2L)).thenReturn(game);

        assertThatThrownBy(() -> dailyEntriesService.addDailyEntry(command))
                .isInstanceOf(InvalidGameNameException.class)
                .hasMessage("Input game name: WhenTaken doesn't match persisted game name: Worldle");

        verify(loadGamePort).loadGame(2L);
        verify(loadPlayerPort, never()).loadPlayer(2L);
        verify(saveDailyEntryPort, never()).save(any());
    }

    @Test
    void shouldFindPlayerByNameWhenPlayerIdIsNull() {
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(
                1L,
                "Mapster",
                null,
                "Player1",
                date,
                950
        );
        DailyEntry savedEntry = new DailyEntry(new DailyEntryId(10L), game, date, player, 950);

        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer("Player1")).thenReturn(player);
        when(saveDailyEntryPort.save(new DailyEntry(null, game, date, player, 950))).thenReturn(savedEntry);

        DailyEntry result = dailyEntriesService.addDailyEntry(command);

        assertThat(result).isEqualTo(savedEntry);

        verify(loadPlayerPort).loadPlayer("Player1");
        verify(loadPlayerPort, never()).loadPlayer(2L);
        verify(saveDailyEntryPort).save(new DailyEntry(null, game, date, player, 950));
    }

    @Test
    void shouldThrowInvalidPlayerNameExceptionWhenInputPlayerNameDoesNotMatchPersistedOne() {
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        AddDailyEntryCommand command = command(
                1L,
                "Mapster",
                2L,
                "Andrzej",
                LocalDate.now().minusDays(10),
                950
        );

        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);

        assertThatThrownBy(() -> dailyEntriesService.addDailyEntry(command))
                .isInstanceOf(InvalidPlayerNameException.class)
                .hasMessage("Input player name: Andrzej doesn't match persisted player name: Player1");

        verify(loadGamePort).loadGame(1L);
        verify(loadPlayerPort).loadPlayer(2L);
        verify(saveDailyEntryPort, never()).save(any());
    }

    @Test
    void shouldSaveNewDailyEntryWithoutDailyEntryId() {
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(
                1L,
                "Mapster",
                2L,
                "Player1",
                date,
                930
        );
        DailyEntry savedEntry = new DailyEntry(new DailyEntryId(10L), game, date, player, 930);

        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);
        when(saveDailyEntryPort.save(any())).thenReturn(savedEntry);

        dailyEntriesService.addDailyEntry(command);

        ArgumentCaptor<DailyEntry> dailyEntryCaptor = ArgumentCaptor.forClass(DailyEntry.class);
        verify(saveDailyEntryPort).save(dailyEntryCaptor.capture());

        assertThat(dailyEntryCaptor.getValue())
                .isEqualTo(new DailyEntry(null, game, date, player, 930));
        assertThat(dailyEntryCaptor.getValue().dailyEntryId()).isNull();
    }

    // ... existing code ...

    private AddDailyEntryCommand command(Long gameId,
                                         String gameName,
                                         Long playerId,
                                         String playerName,
                                         LocalDate date,
                                         int points) {
        return new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(gameId, gameName),
                new AddDailyEntryCommand.PlayerRef(playerId, playerName),
                date,
                points
        );
    }
}
