package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.validation.GameNameValidator;
import com.pretz.geographic.application.domain.validation.InvalidGameNameException;
import com.pretz.geographic.application.domain.validation.InvalidPlayerNameException;
import com.pretz.geographic.application.domain.validation.PlayerNameValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceChainValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidationManager;
import com.pretz.geographic.application.domain.validation.dailyentry.DateValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.GameValidator;
import com.pretz.geographic.application.domain.validation.dailyentry.PlayerValidator;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Mock
    private LoadWeeklyRankingPort loadWeeklyRankingPort;

    @Mock
    private LoadDailyEntriesPort loadDailyEntriesPort;

    private DailyEntriesService dailyEntriesService;

    @BeforeEach
    void setUp() {
        dailyEntriesService = new DailyEntriesService(
                saveDailyEntryPort,
                loadGamePort,
                loadPlayerPort,
                new GameNameValidator(),
                new PlayerNameValidator(),
                new DailyEntryReferenceValidationManager(loadGamePort, loadPlayerPort,
                        new DailyEntryReferenceChainValidator(
                                List.of(new DateValidator(), new GameValidator(), new PlayerValidator())
                        )),
                new DailyEntryContextualValidator(loadWeeklyRankingPort, loadDailyEntriesPort)
        );
    }

    @Test
    void shouldAddDailyEntryWhenGameAndPlayerNamesMatchPersistedOnes() {

        //given
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

        //when
        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);
        when(saveDailyEntryPort.save(new DailyEntry(null, game, date, player, 950))).thenReturn(savedEntry);

        DailyEntry result = dailyEntriesService.addDailyEntry(command);

        //then
        assertThat(result).isEqualTo(savedEntry);

        verify(loadGamePort).loadGame(1L);
        verify(loadPlayerPort).loadPlayer(2L);
        verify(saveDailyEntryPort).save(new DailyEntry(null, game, date, player, 950));
    }

    @Test
    void shouldAddNewDailyEntryWithoutDailyEntryId() {

        //given
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

        //when
        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);
        when(saveDailyEntryPort.save(any())).thenReturn(savedEntry);

        dailyEntriesService.addDailyEntry(command);

        //then
        ArgumentCaptor<DailyEntry> dailyEntryCaptor = ArgumentCaptor.forClass(DailyEntry.class);
        verify(saveDailyEntryPort).save(dailyEntryCaptor.capture());

        assertThat(dailyEntryCaptor.getValue())
                .isEqualTo(new DailyEntry(null, game, date, player, 930));
        assertThat(dailyEntryCaptor.getValue().dailyEntryId()).isNull();
    }

    @Test
    void shouldThrowInvalidGameNameExceptionWhenInputGameNameDoesNotMatchPersistedOne() {

        //given
        Game game = new Game(new GameId(2L), "Worldle", ScoringSystem.STANDARD);
        AddDailyEntryCommand command = command(
                2L,
                "WhenTaken",
                2L,
                "Player1",
                LocalDate.now().minusDays(10),
                950
        );

        //when
        when(loadGamePort.loadGame(2L)).thenReturn(game);

        //when, then
        assertThatThrownBy(() -> dailyEntriesService.addDailyEntry(command))
                .isInstanceOf(InvalidGameNameException.class)
                .hasMessage("Input game name: WhenTaken doesn't match persisted game name: Worldle");

        verify(loadGamePort).loadGame(2L);
        verify(loadPlayerPort, never()).loadPlayer(2L);
        verify(saveDailyEntryPort, never()).save(any());
    }

    //TODO [GEOG-12] should not allow duplicate players names?

    @Test
    void shouldThrowInvalidPlayerNameExceptionWhenInputPlayerNameDoesNotMatchPersistedOne() {

        //given
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

        //when
        when(loadGamePort.loadGame(1L)).thenReturn(game);
        when(loadPlayerPort.loadPlayer(2L)).thenReturn(player);

        //when, then
        assertThatThrownBy(() -> dailyEntriesService.addDailyEntry(command))
                .isInstanceOf(InvalidPlayerNameException.class)
                .hasMessage("Input player name: Andrzej doesn't match persisted player name: Player1");

        verify(loadGamePort).loadGame(1L);
        verify(loadPlayerPort).loadPlayer(2L);
        verify(saveDailyEntryPort, never()).save(any());
    }

    @Test
    void shouldAddAllValidEntriesInBatchAsNewEntries() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(2L), "Player1");
        Player player2 = new Player(new PlayerId(3L), "Player2");
        LocalDate date = LocalDate.now().minusDays(10);

        AddDailyEntryCommand command1 = command(1L, "Mapster", 2L, "Player1", date, 950);
        AddDailyEntryCommand command2 = command(1L, "Mapster", 3L, "Player2", date, 900);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player1, player2));
        stubEmptyContextualLookups();
        stubSaveAllAssigningSequentialIds();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command1, command2));

        //then
        assertThat(result.failureList()).isEmpty();
        assertThat(result.successList()).hasSize(2);
        assertThat(result.successList())
                .extracting(AddDailyEntrySuccess::successCode)
                .containsExactly(AddDailyEntrySuccess.DailyEntrySuccess.NEW, AddDailyEntrySuccess.DailyEntrySuccess.NEW);
        assertThat(result.successList())
                .extracting(it -> it.entry().dailyEntryId())
                .containsExactly(new DailyEntryId(1L), new DailyEntryId(2L));
    }

    @Test
    void shouldReturnUnknownGameFailureWhenGameIdNotFoundInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(99L, "UnknownGame", 2L, "Player1", date, 950);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).failedCommand()).isEqualTo(command);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.UNKNOWN_GAME);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnUnknownGameFailureWhenGameNameDoesNotMatchPersistedOneInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(1L, "WrongGameName", 2L, "Player1", date, 950);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.UNKNOWN_GAME);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnUnknownPlayerFailureWhenPlayerIdNotFoundInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(1L, "Mapster", 99L, "UnknownPlayer", date, 950);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).failedCommand()).isEqualTo(command);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.UNKNOWN_PLAYER);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnUnknownPlayerFailureWhenPlayerNameDoesNotMatchPersistedOneInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(1L, "Mapster", 2L, "WrongName", date, 950);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.UNKNOWN_PLAYER);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnInvalidDateFailureWhenDateIsInFutureInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        AddDailyEntryCommand command = command(1L, "Mapster", 2L, "Player1", futureDate, 950);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.INVALID_DATE);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnInvalidDateFailureWhenDateDoesNotMatchSubmittedAtDayInBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        Instant submittedAtOnDifferentDay = date.minusDays(1).atStartOfDay().toInstant(UTC);
        AddDailyEntryCommand command = commandWithSubmittedAt(1L, "Mapster", 2L, "Player1", date, 950, submittedAtOnDifferentDay);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.INVALID_DATE);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnAListOfFailuresWhenDailyEntryHasManyViolations() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        Instant submittedAtOnDifferentDay = date.minusDays(1).atStartOfDay().toInstant(UTC);
        AddDailyEntryCommand command = commandWithSubmittedAt(3L, "Malpster", 3L, "Bonobo", date, 999, submittedAtOnDifferentDay);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().stream().map(AddDailyEntryFailure::reasons))
                .contains(List.of(AddDailyEntryFailure.Reason.INVALID_DATE,
                        AddDailyEntryFailure.Reason.UNKNOWN_GAME,
                        AddDailyEntryFailure.Reason.UNKNOWN_PLAYER));

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldReturnWeekClosedFailureWhenWeekWasAlreadyCalculated() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate date = LocalDate.now().minusDays(10);
        AddDailyEntryCommand command = command(1L, "Mapster", 2L, "Player1", date, 950);

        DailyEntry temp = new DailyEntry(null, game, date, player, 950);
        GameWeek closedWeek = new GameWeek(game.gameId(), temp.getWeek());

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        when(loadWeeklyRankingPort.loadCalculatedWeeks(any())).thenReturn(Set.of(closedWeek));
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(command));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.WEEK_CLOSED);

        verify(saveDailyEntryPort).saveAll(List.of());
    }

    @Test
    void shouldOnlySaveEntriesThatPassedValidationInMixedBatch() {

        //given
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player = new Player(new PlayerId(2L), "Player1");
        LocalDate validDate = LocalDate.now().minusDays(10);
        LocalDate futureDate = LocalDate.now().plusDays(1);

        AddDailyEntryCommand validCommand = command(1L, "Mapster", 2L, "Player1", validDate, 950);
        AddDailyEntryCommand invalidCommand = command(1L, "Mapster", 2L, "Player1", futureDate, 900);

        //when
        when(loadGamePort.loadGames(any())).thenReturn(List.of(game));
        when(loadPlayerPort.loadPlayers(any())).thenReturn(List.of(player));
        stubEmptyContextualLookups();
        stubSaveAllAssigningSequentialIds();

        AddDailyEntriesResult result = dailyEntriesService.addDailyEntries(List.of(validCommand, invalidCommand));

        //then
        assertThat(result.successList()).hasSize(1);
        assertThat(result.successList().get(0).entry().points()).isEqualTo(950);
        assertThat(result.failureList()).hasSize(1);
        assertThat(result.failureList().get(0).reasons()).containsExactly(AddDailyEntryFailure.Reason.INVALID_DATE);

        ArgumentCaptor<List<DailyEntry>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(saveDailyEntryPort).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
        assertThat(savedCaptor.getValue().get(0).points()).isEqualTo(950);
    }

    //TODO duplicates test

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
                points,
                date.atStartOfDay().toInstant(UTC));
    }

    private AddDailyEntryCommand commandWithSubmittedAt(Long gameId,
                                                        String gameName,
                                                        Long playerId,
                                                        String playerName,
                                                        LocalDate date,
                                                        int points,
                                                        Instant submittedAt) {
        return new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(gameId, gameName),
                new AddDailyEntryCommand.PlayerRef(playerId, playerName),
                date,
                points,
                submittedAt);
    }

    private void stubEmptyContextualLookups() {
        when(loadWeeklyRankingPort.loadCalculatedWeeks(any())).thenReturn(Set.of());
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());
    }

    private void stubSaveAllAssigningSequentialIds() {
        when(saveDailyEntryPort.saveAll(anyList())).thenAnswer(invocation -> {
            List<DailyEntry> toSave = invocation.getArgument(0);
            return IntStream.range(0, toSave.size())
                    .mapToObj(i -> new DailyEntry(new DailyEntryId((long) (i + 1)),
                            toSave.get(i).game(), toSave.get(i).date(),
                            toSave.get(i).player(), toSave.get(i).points(), toSave.get(i).submittedAt()))
                    .toList();
        });
    }
}

