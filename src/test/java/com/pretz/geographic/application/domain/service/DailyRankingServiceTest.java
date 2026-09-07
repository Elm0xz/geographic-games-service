package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.validation.InvalidDateException;
import com.pretz.geographic.application.domain.validation.RankingDateValidator;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRankingServiceTest {

    @Mock
    private LoadGamePort loadGamePort;

    @Mock
    private LoadDailyEntriesPort loadDailyEntriesPort;

    private DailyRankingService dailyRankingService;

    @BeforeEach
    void setUp() {
        dailyRankingService = new DailyRankingService(loadGamePort, loadDailyEntriesPort, new RankingDateValidator());
    }

    @Test
    void shouldReturnDailyRankingsForPastDate() {

        //given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Game game1 = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Game game2 = new Game(new GameId(2L), "WhenTaken", ScoringSystem.STANDARD);

        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        DailyEntry entry1 = new DailyEntry(new DailyEntryId(1L), game1, pastDate, player1, 950);
        DailyEntry entry2 = new DailyEntry(new DailyEntryId(2L), game1, pastDate, player2, 970);
        DailyEntry entry3 = new DailyEntry(new DailyEntryId(3L), game2, pastDate, player1, 888);
        DailyEntry entry4 = new DailyEntry(new DailyEntryId(4L), game2, pastDate, player2, 864);
        DailyRanking ranking1 = DailyRanking.of(game1, pastDate, List.of(entry2, entry1));
        DailyRanking ranking2 = DailyRanking.of(game2, pastDate, List.of(entry3, entry4));

        //when
        when(loadGamePort.loadActiveGames()).thenReturn(List.of(game1, game2));
        when(loadDailyEntriesPort.loadEntries(List.of(game1, game2), pastDate)).thenReturn(List.of(entry1, entry2, entry3, entry4));

        List<DailyRanking> result = dailyRankingService.getDailyRankings(pastDate);

        //then
        assertThat(result).containsExactly(ranking1, ranking2);
    }

    @Test
    void shouldReturnDailyRankingsForListOfGamesAndDateInterval() {

        //given
        Game game1 = new Game(new GameId(1L), "WhenTaken", ScoringSystem.STANDARD);
        Game game2 = new Game(new GameId(2L), "GeoSplit", ScoringSystem.STANDARD);

        List<Game> inputGames = List.of(game1, game2);

        LocalDate monday = LocalDate.now().minusDays(10);
        LocalDate tuesday = monday.plusDays(1);
        LocalDate friday = monday.plusDays(4);
        LocalDate sunday = monday.plusDays(6);

        Player player1 = new Player(new PlayerId(1L), "Alice");
        Player player2 = new Player(new PlayerId(2L), "Bob");
        Player player3 = new Player(new PlayerId(3L), "Charlie");

        // Game 1 (WhenTaken) entries
        DailyEntry e1Mon1 = new DailyEntry(new DailyEntryId(21L), game1, monday, player1, 950);
        DailyEntry e1Mon2 = new DailyEntry(new DailyEntryId(22L), game1, monday, player2, 870);
        DailyEntry e1Mon3 = new DailyEntry(new DailyEntryId(23L), game1, monday, player3, 910);
        DailyEntry e1Tue1 = new DailyEntry(new DailyEntryId(24L), game1, tuesday, player1, 880);
        DailyEntry e1Tue2 = new DailyEntry(new DailyEntryId(25L), game1, tuesday, player2, 930);
        DailyEntry e1Fri1 = new DailyEntry(new DailyEntryId(26L), game1, friday, player2, 900);
        DailyEntry e1Fri2 = new DailyEntry(new DailyEntryId(27L), game1, friday, player3, 860);
        DailyEntry e1Sun1 = new DailyEntry(new DailyEntryId(28L), game1, sunday, player1, 920);
        DailyEntry e1Sun2 = new DailyEntry(new DailyEntryId(29L), game1, sunday, player3, 975);

        // Game 2 (GeoSplit) entries
        DailyEntry e2Mon1 = new DailyEntry(new DailyEntryId(31L), game2, monday, player1, 800);
        DailyEntry e2Mon2 = new DailyEntry(new DailyEntryId(32L), game2, monday, player3, 840);
        DailyEntry e2Tue1 = new DailyEntry(new DailyEntryId(33L), game2, tuesday, player2, 810);
        DailyEntry e2Tue2 = new DailyEntry(new DailyEntryId(34L), game2, tuesday, player3, 795);
        DailyEntry e2Fri1 = new DailyEntry(new DailyEntryId(35L), game2, friday, player1, 860);
        DailyEntry e2Fri2 = new DailyEntry(new DailyEntryId(36L), game2, friday, player2, 825);
        DailyEntry e2Fri3 = new DailyEntry(new DailyEntryId(37L), game2, friday, player3, 845);
        DailyEntry e2Sun1 = new DailyEntry(new DailyEntryId(38L), game2, sunday, player1, 790);
        DailyEntry e2Sun2 = new DailyEntry(new DailyEntryId(39L), game2, sunday, player2, 830);

        DailyRanking rankG2Mon = DailyRanking.of(game2, monday, List.of(e2Mon1, e2Mon2));
        DailyRanking rankG2Tue = DailyRanking.of(game2, tuesday, List.of(e2Tue1, e2Tue2));
        DailyRanking rankG2Fri = DailyRanking.of(game2, friday, List.of(e2Fri1, e2Fri2, e2Fri3));
        DailyRanking rankG2Sun = DailyRanking.of(game2, sunday, List.of(e2Sun1, e2Sun2));
        DailyRanking rankG1Mon = DailyRanking.of(game1, monday, List.of(e1Mon1, e1Mon2, e1Mon3));
        DailyRanking rankG1Tue = DailyRanking.of(game1, tuesday, List.of(e1Tue1, e1Tue2));
        DailyRanking rankG1Fri = DailyRanking.of(game1, friday, List.of(e1Fri1, e1Fri2));
        DailyRanking rankG1Sun = DailyRanking.of(game1, sunday, List.of(e1Sun1, e1Sun2));

        when(loadGamePort.loadGames(List.of(game1.gameId().id(), game2.gameId().id())))
                .thenReturn(List.of(game1, game2));
        when(loadDailyEntriesPort.loadEntries(List.of(game1, game2), monday, sunday))
                .thenReturn(List.of(
                        e1Mon1, e1Mon2, e1Mon3, e1Tue1, e1Tue2, e1Fri1, e1Fri2, e1Sun1, e1Sun2,
                        e2Mon1, e2Mon2, e2Tue1, e2Tue2, e2Fri1, e2Fri2, e2Fri3, e2Sun1, e2Sun2));

        //when
        List<DailyRanking> result = dailyRankingService.getDailyRankings(monday, sunday, inputGames);

        //then
        assertThat(result).containsExactly(
                rankG2Mon, rankG2Tue, rankG2Fri, rankG2Sun,
                rankG1Mon, rankG1Tue, rankG1Fri, rankG1Sun);
    }

    @Test
    void shouldThrowInvalidDateExceptionWhenDateIsToday() {

        //given
        LocalDate today = LocalDate.now();

        //when, then
        assertThatThrownBy(() -> dailyRankingService.getDailyRankings(today))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining(today.toString());
    }

    @Test
    void shouldThrowInvalidDateExceptionWhenDateIsInFuture() {

        //given
        LocalDate futureDate = LocalDate.now().plusDays(1);

        //when, then
        assertThatThrownBy(() -> dailyRankingService.getDailyRankings(futureDate))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining(futureDate.toString());
    }

    @MethodSource("invalidDates")
    @ParameterizedTest
    void shouldThrowInvalidDateExceptionForDateIntervalInputWhenEndDateIsNotInPast(LocalDate to) {

        //given
        LocalDate from = to.minusDays(7);

        Game game1 = new Game(new GameId(1L), "WhenTaken", ScoringSystem.STANDARD);
        Game game2 = new Game(new GameId(2L), "GeoSplit", ScoringSystem.STANDARD);

        List<Game> inputGames = List.of(game1, game2);

        //when, then
        assertThatThrownBy(() -> dailyRankingService.getDailyRankings(from, to, inputGames))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining(to.toString());
    }

    private static Stream<Arguments> invalidDates() {
        return Stream.of(
                Arguments.of(LocalDate.now()),
                Arguments.of(LocalDate.now().plusDays(2))
        );
    }
}
