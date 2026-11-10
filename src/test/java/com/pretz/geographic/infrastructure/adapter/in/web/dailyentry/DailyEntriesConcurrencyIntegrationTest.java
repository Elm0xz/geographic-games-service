package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.jayway.jsonpath.JsonPath;
import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import com.pretz.geographic.infrastructure.adapter.in.web.AbstractWebIntegrationTest;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry.DailyEntryPersistenceAdapter;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaEntity;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Two requests race on the same natural keys. {@link GatedSaveDailyEntryPort} parks each request after it has read
 * the persisted entries and before it writes, so both requests always decide on the same (soon stale) snapshot.
 * The test then controls whether the writes run one after the other (first request fully committed before the
 * second writes) or are released together.
 */
class DailyEntriesConcurrencyIntegrationTest extends AbstractWebIntegrationTest {

    private static final String ENDPOINT = "/api/daily-entries";
    private static final long TIMEOUT_SECONDS = 15;
    private static final LocalDate DATE = LocalDate.of(2026, 7, 29);

    private static final String EARLIER = "2026-07-29T08:00:00Z";
    private static final String LATER = "2026-07-29T10:00:00Z";
    private static final String STORED = "2026-07-29T12:00:00Z";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameJpaRepository gameJpaRepository;

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private DailyEntryJpaRepository dailyEntryJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private GameJpaEntity savedGame;
    private PlayerJpaEntity savedPlayer1;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE daily_entry, game, player RESTART IDENTITY CASCADE");
        savedGame = gameJpaRepository.save(new GameJpaEntity("Mapster", ScoringSystem.STANDARD));
        savedPlayer1 = playerJpaRepository.save(new PlayerJpaEntity("Player1"));
        playerJpaRepository.save(new PlayerJpaEntity("Player2"));
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void insertRaceWhenEarlierBatchCommitsFirstShouldConflictAndRetryShouldBeSuperseded() throws Exception {

        var earlier = batch(entry(1, 990, EARLIER));
        var later = batch(entry(1, 950, LATER));

        var race = raceSequentially(earlier, later);

        assertThat(race.first().status()).isEqualTo(200);
        assertThat(race.first().successCodes()).containsExactly("NEW");
        assertThat(race.second().status()).isEqualTo(409);
        assertThat(race.second().errorCode()).isEqualTo("CONCURRENT_MODIFICATION");

        var retry = send(later);
        assertThat(retry.status()).isEqualTo(200);
        assertThat(retry.successCodes()).isEmpty();
        assertThat(retry.failureCodes()).containsExactly(List.of("SUPERSEDED"));

        assertStoredEntry(1, 990, EARLIER);
    }

    @Test
    void insertRaceWhenLaterBatchCommitsFirstShouldConflictAndRetryShouldCorrect() throws Exception {

        var earlier = batch(entry(1, 990, EARLIER));
        var later = batch(entry(1, 950, LATER));

        var race = raceSequentially(later, earlier);

        assertThat(race.first().status()).isEqualTo(200);
        assertThat(race.first().successCodes()).containsExactly("NEW");
        assertThat(race.second().status()).isEqualTo(409);
        assertThat(race.second().errorCode()).isEqualTo("CONCURRENT_MODIFICATION");

        var retry = send(earlier);
        assertThat(retry.status()).isEqualTo(200);
        assertThat(retry.successCodes()).containsExactly("CORRECTED");

        assertStoredEntry(1, 990, EARLIER);
    }

    @Test
    void updateRaceWhenLaterBatchCommitsFirstShouldLetEarlierBatchWinWithoutRetry() throws Exception {

        storeEntry(800, STORED);
        var earlier = batch(entry(1, 990, EARLIER));
        var later = batch(entry(1, 950, LATER));

        var race = raceSequentially(later, earlier);

        assertThat(race.first().status()).isEqualTo(200);
        assertThat(race.first().successCodes()).containsExactly("CORRECTED");
        assertThat(race.second().status()).isEqualTo(200);
        assertThat(race.second().successCodes()).containsExactly("CORRECTED");

        assertStoredEntry(1, 990, EARLIER);
    }

    @Test
    void updateRaceWhenEarlierBatchCommitsFirstShouldConflictAndRetryShouldBeSuperseded() throws Exception {

        storeEntry(800, STORED);
        var earlier = batch(entry(1, 990, EARLIER));
        var later = batch(entry(1, 950, LATER));

        var race = raceSequentially(earlier, later);

        assertThat(race.first().status()).isEqualTo(200);
        assertThat(race.first().successCodes()).containsExactly("CORRECTED");
        assertThat(race.second().status()).isEqualTo(409);
        assertThat(race.second().errorCode()).isEqualTo("CONCURRENT_MODIFICATION");
        assertStoredEntry(1, 990, EARLIER);

        var retry = send(later);
        assertThat(retry.status()).isEqualTo(200);
        assertThat(retry.failureCodes()).containsExactly(List.of("SUPERSEDED"));

        assertStoredEntry(1, 990, EARLIER);
    }

    @Test
    void overlappingKeysInOppositeOrderShouldSucceedOrConflictButNeverFail() throws Exception {

        var batchA = batch(entry(1, 990, EARLIER), entry(2, 991, EARLIER));
        var batchB = batch(entry(2, 951, LATER), entry(1, 950, LATER));

        var race = raceConcurrently(batchA, batchB);

        assertThat(List.of(race.first().status(), race.second().status()))
                .as("statuses of both racing requests")
                .allMatch(status -> status == 200 || status == 409)
                .contains(200);

        // whoever lost retries; the keys converge on the earliest submission regardless of who won the race
        for (var outcome : List.of(race.first(), race.second())) {
            if (outcome.status() == 409) {
                assertThat(send(outcome.requestBody()).status()).isEqualTo(200);
            }
        }
        assertStoredEntry(1, 990, EARLIER);
        assertStoredEntry(2, 991, EARLIER);
    }

    private Race raceSequentially(String firstBody, String secondBody) throws Exception {
        return race(firstBody, secondBody, true);
    }

    private Race raceConcurrently(String firstBody, String secondBody) throws Exception {
        return race(firstBody, secondBody, false);
    }

    private Race race(String firstBody, String secondBody, boolean secondWritesAfterFirstCommits) throws Exception {
        var bothRead = new CountDownLatch(2);
        var firstFinished = new CountDownLatch(1);

        Future<Outcome> first = executor.submit(() -> {
            try {
                GatedSaveDailyEntryPort.beforeSaveInCurrentThread(() -> arriveAndAwait(bothRead));
                return send(firstBody);
            } finally {
                GatedSaveDailyEntryPort.clearCurrentThread();
                firstFinished.countDown();
            }
        });
        Future<Outcome> second = executor.submit(() -> {
            try {
                GatedSaveDailyEntryPort.beforeSaveInCurrentThread(() -> {
                    arriveAndAwait(bothRead);
                    if (secondWritesAfterFirstCommits) {
                        await(firstFinished);
                    }
                });
                return send(secondBody);
            } finally {
                GatedSaveDailyEntryPort.clearCurrentThread();
            }
        });

        return new Race(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private static void arriveAndAwait(CountDownLatch latch) {
        latch.countDown();
        await(latch);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for the other request");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private Outcome send(String body) throws Exception {
        var response = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse();
        return new Outcome(body, response.getStatus(), response.getContentAsString());
    }

    private void storeEntry(int points, String submittedAt) {
        dailyEntryJpaRepository.save(new DailyEntryJpaEntity(savedGame, savedPlayer1, DATE, points, Instant.parse(submittedAt)));
    }

    private void assertStoredEntry(long playerId, int points, String submittedAt) {
        var stored = dailyEntryJpaRepository.findAll().stream()
                .filter(it -> it.getPlayer().getId() == playerId)
                .toList();
        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst())
                .extracting(DailyEntryJpaEntity::getPoints, DailyEntryJpaEntity::getSubmittedAt)
                .containsExactly(points, Instant.parse(submittedAt));
    }

    private static String batch(String... entries) {
        return """
                {
                  "createDailyEntryRequestDtos": [%s]
                }
                """.formatted(String.join(",", entries));
    }

    private static String entry(long playerId, int points, String submittedAt) {
        return """
                {
                  "game": { "id": 1, "name": "Mapster" },
                  "player": { "id": %d, "name": "Player%d" },
                  "date": "%s",
                  "points": %d,
                  "submittedAt": "%s"
                }
                """.formatted(playerId, playerId, DATE, points, submittedAt);
    }

    private record Race(Outcome first, Outcome second) {
    }

    private record Outcome(String requestBody, int status, String responseBody) {

        List<String> successCodes() {
            return JsonPath.read(responseBody, "$.createDailyEntrySuccessResponseDtoList[*].successCode");
        }

        List<List<String>> failureCodes() {
            return JsonPath.read(responseBody, "$.createDailyEntryFailureResponseDtoList[*].failureCode");
        }

        String errorCode() {
            return JsonPath.read(responseBody, "$.code");
        }
    }

    @TestConfiguration
    static class GateConfiguration {

        @Bean
        @Primary
        GatedSaveDailyEntryPort gatedSaveDailyEntryPort(DailyEntryPersistenceAdapter adapter) {
            return new GatedSaveDailyEntryPort(adapter);
        }
    }

    /**
     * Decorates the real save port with a per-thread hook that runs between the service's read and its write.
     * MockMvc handles a request on the calling thread, so a thread-local hook targets exactly one request.
     */
    static class GatedSaveDailyEntryPort implements SaveDailyEntryPort {

        private static final ThreadLocal<Runnable> BEFORE_SAVE = new ThreadLocal<>();

        private final SaveDailyEntryPort delegate;

        GatedSaveDailyEntryPort(SaveDailyEntryPort delegate) {
            this.delegate = delegate;
        }

        static void beforeSaveInCurrentThread(Runnable hook) {
            BEFORE_SAVE.set(hook);
        }

        static void clearCurrentThread() {
            BEFORE_SAVE.remove();
        }

        @Override
        public List<DailyEntry> saveAll(List<DailyEntry> entries) {
            var hook = BEFORE_SAVE.get();
            if (hook != null) {
                hook.run();
            }
            return delegate.saveAll(entries);
        }
    }
}
