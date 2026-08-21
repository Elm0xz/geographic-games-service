package com.pretz.geographic.application.domain;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.DailyRanking;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.service.DailyRankingCalculator;
import com.pretz.geographic.application.domain.validation.InvalidDateException;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.LoadGamePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRankingServiceTest {

    @Mock
    private LoadGamePort loadGamePort;

    @Mock
    private LoadDailyEntriesPort loadDailyEntriesPort;

    @Mock
    private DailyRankingCalculator dailyRankingCalculator;

    private DailyRankingService dailyRankingService;

    @BeforeEach
    void setUp() {
        dailyRankingService = new DailyRankingService(loadGamePort, loadDailyEntriesPort, dailyRankingCalculator);
    }

    @Test
    void shouldReturnDailyRankingsForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Game game = new Game(new GameId(1L), "Mapster", ScoringSystem.STANDARD);
        Player player1 = new Player(new PlayerId(1L), "Player1");
        Player player2 = new Player(new PlayerId(2L), "Player2");
        DailyEntry entry1 = new DailyEntry(new DailyEntryId(1L), game, pastDate, player1, 950);
        DailyEntry entry2 = new DailyEntry(new DailyEntryId(2L), game, pastDate, player2, 970);
        DailyRanking ranking = new DailyRanking(game, pastDate, List.of(entry2, entry1));

        when(loadGamePort.loadActiveGames()).thenReturn(List.of(game));
        when(loadDailyEntriesPort.loadEntries(List.of(game), pastDate)).thenReturn(List.of(entry1, entry2));
        when(dailyRankingCalculator.calculateDailyRanking(List.of(entry1, entry2), game, pastDate)).thenReturn(ranking);

        List<DailyRanking> result = dailyRankingService.getDailyRankings(pastDate);

        assertThat(result).containsExactly(ranking);
    }

    @Test
    void shouldThrowInvalidDateExceptionWhenDateIsToday() {
        LocalDate today = LocalDate.now();

        assertThatThrownBy(() -> dailyRankingService.getDailyRankings(today))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining(today.toString());
    }

    @Test
    void shouldThrowInvalidDateExceptionWhenDateIsInFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> dailyRankingService.getDailyRankings(futureDate))
                .isInstanceOf(InvalidDateException.class)
                .hasMessageContaining(futureDate.toString());
    }
}
