package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.SUPERSEDED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure.Reason.UNKNOWN_GAME;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.CORRECTED;
import static com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess.DailyEntrySuccess.NEW;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyEntryDuplicatesResolverTest {

    private static final LocalDate DATE = LocalDate.of(2026, 7, 29);
    private static final Instant EARLY = DATE.atTime(8, 0).toInstant(UTC);
    private static final Instant MIDDLE = DATE.atTime(12, 0).toInstant(UTC);
    private static final Instant LATE = DATE.atTime(18, 0).toInstant(UTC);

    private final Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
    private final Player player1 = new Player(new PlayerId(1L), "Player1");
    private final Player player2 = new Player(new PlayerId(2L), "Player2");

    @Mock
    private LoadDailyEntriesPort loadDailyEntriesPort;

    private DailyEntryDuplicatesResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DailyEntryDuplicatesResolver(loadDailyEntriesPort);
    }

    @Test
    void shouldMarkAllEntriesAsNewWhenThereAreNoDuplicates() {

        //given
        DailyEntry entry1 = entry(player1, DATE, 950, MIDDLE);
        DailyEntry entry2 = entry(player2, DATE, 900, MIDDLE);
        DailyEntry entry3 = entry(player1, DATE.minusDays(1), 800, DATE.minusDays(1).atStartOfDay().toInstant(UTC));

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(entry1, entry2, entry3));

        //then
        assertThat(result.failureList()).isEmpty();
        assertThat(result.successList()).containsExactly(
                new AddDailyEntrySuccess(entry1, NEW),
                new AddDailyEntrySuccess(entry2, NEW),
                new AddDailyEntrySuccess(entry3, NEW));
    }

    @Test
    void shouldPassThroughFailuresFromPreviousValidationSteps() {

        //given
        DailyEntry entry = entry(player1, DATE, 950, MIDDLE);
        AddDailyEntryFailure previousFailure = new AddDailyEntryFailure(
                new AddDailyEntryCommand(
                        new AddDailyEntryCommand.GameRef(99L, "Unknown"),
                        new AddDailyEntryCommand.PlayerRef(1L, "Player1"),
                        DATE, 900, MIDDLE),
                List.of(UNKNOWN_GAME));

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(
                new ValidationIntermediateResult(List.of(entry), List.of(previousFailure)));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(entry, NEW));
        assertThat(result.failureList()).containsExactly(previousFailure);
    }

    @Test
    void shouldSupersedeLaterBatchEntryWhenEarlierOneArrivesFirst() {

        //given
        DailyEntry earlier = entry(player1, DATE, 950, EARLY);
        DailyEntry later = entry(player1, DATE, 900, LATE);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(earlier, later));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(earlier, NEW));
        assertThat(result.failureList()).containsExactly(superseded(later));
    }

    @Test
    void shouldSupersedeLaterBatchEntryWhenEarlierOneArrivesSecond() {

        //given
        DailyEntry later = entry(player1, DATE, 900, LATE);
        DailyEntry earlier = entry(player1, DATE, 950, EARLY);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(later, earlier));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(earlier, NEW));
        assertThat(result.failureList()).containsExactly(superseded(later));
    }

    @Test
    void shouldKeepFirstBatchEntryWhenDuplicatesHaveEqualTimestamps() {

        //given
        DailyEntry first = entry(player1, DATE, 950, MIDDLE);
        DailyEntry second = entry(player1, DATE, 900, MIDDLE);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(first, second));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(first, NEW));
        assertThat(result.failureList()).containsExactly(superseded(second));
    }

    @Test
    void shouldKeepEarliestOfManyBatchDuplicates() {

        //given
        DailyEntry middle = entry(player1, DATE, 900, MIDDLE);
        DailyEntry late = entry(player1, DATE, 850, LATE);
        DailyEntry early = entry(player1, DATE, 950, EARLY);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of());

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(middle, late, early));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(early, NEW));
        assertThat(result.failureList()).containsExactlyInAnyOrder(superseded(middle), superseded(late));
    }

    @Test
    void shouldCorrectPersistedEntryWhenArrivingEntryIsEarlier() {

        //given
        DailyEntry persisted = persisted(player1, 900, LATE);
        DailyEntry arriving = entry(player1, DATE, 950, EARLY);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of(persisted));

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(arriving));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(
                persisted(player1, 950, EARLY), CORRECTED));
        assertThat(result.failureList()).isEmpty();
    }

    @Test
    void shouldSupersedeArrivingEntryWhenPersistedEntryIsEarlier() {

        //given
        DailyEntry persisted = persisted(player1, 900, EARLY);
        DailyEntry arriving = entry(player1, DATE, 950, LATE);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of(persisted));

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(arriving));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).containsExactly(superseded(arriving));
    }

    @Test
    void shouldSupersedeArrivingEntryWhenPersistedEntryHasEqualTimestamp() {

        //given
        DailyEntry persisted = persisted(player1, 900, MIDDLE);
        DailyEntry arriving = entry(player1, DATE, 950, MIDDLE);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of(persisted));

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(arriving));

        //then
        assertThat(result.successList()).isEmpty();
        assertThat(result.failureList()).containsExactly(superseded(arriving));
    }

    @Test
    void shouldCorrectPersistedEntryWithEarliestOfManyBatchDuplicates() {

        //given
        DailyEntry persisted = persisted(player1, 800, LATE);
        DailyEntry middle = entry(player1, DATE, 900, MIDDLE);
        DailyEntry early = entry(player1, DATE, 950, EARLY);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of(persisted));

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(middle, early));

        //then
        assertThat(result.successList()).containsExactly(new AddDailyEntrySuccess(
                persisted(player1, 950, EARLY), CORRECTED));
        assertThat(result.failureList()).containsExactly(superseded(middle));
    }

    @Test
    void shouldResolveMixedBatchKeepingBatchOrderOfSuccesses() {

        //given
        DailyEntry persisted = persisted(player2, 800, LATE);
        DailyEntry newEntry = entry(player1, DATE, 950, MIDDLE);
        DailyEntry correction = entry(player2, DATE, 900, EARLY);
        DailyEntry duplicate = entry(player1, DATE, 700, LATE);

        //when
        when(loadDailyEntriesPort.loadEntries(anyList())).thenReturn(List.of(persisted));

        AddDailyEntriesResult result = resolver.resolveDuplicates(intermediateWithPassed(newEntry, correction, duplicate));

        //then
        assertThat(result.successList()).containsExactly(
                new AddDailyEntrySuccess(newEntry, NEW),
                new AddDailyEntrySuccess(persisted(player2, 900, EARLY), CORRECTED));
        assertThat(result.failureList()).containsExactly(superseded(duplicate));
    }

    private DailyEntry entry(Player player, LocalDate date, int points, Instant submittedAt) {
        return new DailyEntry(null, game, date, player, points, submittedAt);
    }

    private DailyEntry persisted(Player player, int points, Instant submittedAt) {
        return new DailyEntry(new DailyEntryId(10L), game, DATE, player, points, submittedAt);
    }

    private ValidationIntermediateResult intermediateWithPassed(DailyEntry... entriesPassed) {
        return new ValidationIntermediateResult(List.of(entriesPassed), List.of());
    }

    private AddDailyEntryFailure superseded(DailyEntry entry) {
        return new AddDailyEntryFailure(new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(entry.game().gameId().id(), entry.game().name()),
                new AddDailyEntryCommand.PlayerRef(entry.player().playerId().id(), entry.player().name()),
                entry.date(),
                entry.points(),
                entry.submittedAt()), List.of(SUPERSEDED));
    }
}
