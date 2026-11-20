package com.pretz.geographic.infrastructure.adapter.out.persistence.weeklyranking;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.domain.model.PlayerId;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.domain.model.WeeklyPosition;
import com.pretz.geographic.application.domain.model.WeeklyRanking;
import com.pretz.geographic.infrastructure.adapter.out.persistence.AbstractPostgresDataJpaTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GamePersistenceMapper;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@Import({WeeklyRankingPersistenceAdapter.class, WeeklyRankingPersistenceMapper.class,
        GamePersistenceMapper.class, PlayerPersistenceMapper.class})
class WeeklyRankingPersistenceAdapterTest extends AbstractPostgresDataJpaTest {

    @Autowired
    private WeeklyRankingPersistenceAdapter adapter;

    @Autowired
    private WeeklyRankingPersistenceMapper mapper;

    @Autowired
    private WeeklyRankingJpaRepository weeklyRankingRepository;

    @Autowired
    private GameJpaRepository gameRepository;

    @Autowired
    private PlayerJpaRepository playerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Game game1;
    private Game game2;
    private Player player1;
    private Player player2;

    private final Week week = Week.of(2026, 4);

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("TRUNCATE TABLE weekly_position, weekly_ranking, game, player RESTART IDENTITY CASCADE");

        GameJpaEntity savedGame1 = gameRepository.save(new GameJpaEntity("Game1", ScoringSystem.STANDARD));
        GameJpaEntity savedGame2 = gameRepository.save(new GameJpaEntity("Game2", ScoringSystem.STANDARD));
        PlayerJpaEntity savedPlayer1 = playerRepository.save(new PlayerJpaEntity("Player1"));
        PlayerJpaEntity savedPlayer2 = playerRepository.save(new PlayerJpaEntity("Player2"));

        game1 = new Game(new GameId(savedGame1.getId()), savedGame1.getName(), savedGame1.getScoringSystem());
        game2 = new Game(new GameId(savedGame2.getId()), savedGame2.getName(), savedGame2.getScoringSystem());
        player1 = new Player(new PlayerId(savedPlayer1.getId()), savedPlayer1.getName());
        player2 = new Player(new PlayerId(savedPlayer2.getId()), savedPlayer2.getName());
    }

    @Test
    void shouldLoadWeeklyRankings() {
        //given
        WeeklyRanking ranking = rankingWithPositions(game1, week, player1, player2);
        weeklyRankingRepository.save(mapper.toEntity(ranking, gameRepository::getReferenceById, playerRepository::getReferenceById));

        //when
        List<WeeklyRanking> result = adapter.loadWeeklyRankings(List.of(game1), week);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(ranking);
    }

    @Test
    void shouldReturnEmptyListWhenNoRankingExistsForGivenWeek() {
        //given - no ranking persisted

        //when
        List<WeeklyRanking> result = adapter.loadWeeklyRankings(List.of(game1), week);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnPartialListWhenOnlyOneOfTwoGamesHasRankingForGivenWeek() {
        //given
        WeeklyRanking ranking1 = rankingWithPositions(game1, week, player1, player2);
        weeklyRankingRepository.save(mapper.toEntity(ranking1, gameRepository::getReferenceById, playerRepository::getReferenceById));

        //when
        List<WeeklyRanking> result = adapter.loadWeeklyRankings(List.of(game1, game2), week);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().game()).isEqualTo(game1);
    }

    @Test
    void shouldLoadOnlyRankingsWithinWeekIntervalSpanningYearBoundary() {
        //given
        Week beforeInterval = Week.of(2020, 51);
        Week intervalStart = Week.of(2020, 52);
        Week lastWeekOf2020 = Week.of(2020, 53);
        Week intervalEnd = Week.of(2021, 1);
        Week afterInterval = Week.of(2021, 2);
        WeeklyRanking startRanking = rankingWithPositions(game1, intervalStart, player1, player2);
        WeeklyRanking middleRanking = rankingWithPositions(game2, lastWeekOf2020, player1, player2);
        WeeklyRanking endRanking = rankingWithPositions(game1, intervalEnd, player1, player2);
        List.of(rankingWithPositions(game1, beforeInterval, player1, player2), startRanking, middleRanking, endRanking,
                        rankingWithPositions(game1, afterInterval, player1, player2))
                .forEach(it -> weeklyRankingRepository.save(
                        mapper.toEntity(it, gameRepository::getReferenceById, playerRepository::getReferenceById)));

        //when
        List<WeeklyRanking> result = adapter.loadWeeklyRankings(List.of(game1, game2), intervalStart, intervalEnd);

        //then
        assertThat(result).containsExactlyInAnyOrder(startRanking, middleRanking, endRanking);
    }

    @Test
    void shouldLoadRankingsOnlyForRequestedGamesWithinWeekInterval() {
        //given
        Week from = Week.of(2026, 3);
        WeeklyRanking game1Ranking = rankingWithPositions(game1, from, player1, player2);
        WeeklyRanking game2Ranking = rankingWithPositions(game2, week, player1, player2);
        List.of(game1Ranking, game2Ranking).forEach(it -> weeklyRankingRepository.save(
                mapper.toEntity(it, gameRepository::getReferenceById, playerRepository::getReferenceById)));

        //when
        List<WeeklyRanking> result = adapter.loadWeeklyRankings(List.of(game2), from, week);

        //then
        assertThat(result).containsExactly(game2Ranking);
    }

    @Test
    void shouldLoadOnlyRequestedCalculatedWeeksWhenGameWeeksSpanYearBoundary() {
        //given
        Week lastWeekOf2020 = Week.of(2020, 53);
        Week firstWeekOf2021 = Week.of(2021, 1);
        List.of(rankingWithPositions(game1, lastWeekOf2020, player1, player2),
                        rankingWithPositions(game1, firstWeekOf2021, player1, player2),
                        // matches requested game, year and week independently, but wasn't requested
                        rankingWithPositions(game1, Week.of(2020, 1), player1, player2))
                .forEach(it -> weeklyRankingRepository.save(
                        mapper.toEntity(it, gameRepository::getReferenceById, playerRepository::getReferenceById)));

        //when
        Set<GameWeek> result = adapter.loadCalculatedWeeks(List.of(
                new GameWeek(game1.gameId(), lastWeekOf2020),
                new GameWeek(game1.gameId(), firstWeekOf2021),
                new GameWeek(game2.gameId(), firstWeekOf2021)));

        //then
        assertThat(result).containsExactlyInAnyOrder(
                new GameWeek(game1.gameId(), lastWeekOf2020),
                new GameWeek(game1.gameId(), firstWeekOf2021));
    }

    @Test
    void shouldSaveAndReturnWeeklyRankings() {
        //given
        WeeklyRanking ranking = rankingWithPositions(game1, week, player1, player2);

        //when
        List<WeeklyRanking> result = adapter.save(List.of(ranking));

        //then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(ranking);
        assertThat(weeklyRankingRepository.findAll()).hasSize(1);
    }

    private WeeklyRanking rankingWithPositions(Game game, Week week, Player... players) {
        List<WeeklyPosition> positions = List.of(
                new WeeklyPosition(game, week, players[0], 3, 2700),
                new WeeklyPosition(game, week, players[1], 1, 1800));
        return WeeklyRanking.of(game, week, positions);
    }
}