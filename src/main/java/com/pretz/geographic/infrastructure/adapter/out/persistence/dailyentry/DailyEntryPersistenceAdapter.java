package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyEntryId;
import com.pretz.geographic.application.domain.model.DailyRankingKey;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import com.pretz.geographic.application.port.out.exception.ConcurrentModificationConflictException;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class DailyEntryPersistenceAdapter implements LoadDailyEntriesPort, SaveDailyEntryPort {

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";
    private static final String CONFLICT_MESSAGE = "Concurrent modification conflict while saving daily entries";

    private final DailyEntryJpaRepository dailyEntryRepository;
    private final GameJpaRepository gameRepository;
    private final PlayerJpaRepository playerRepository;
    private final DailyEntryPersistenceMapper mapper;

    DailyEntryPersistenceAdapter(DailyEntryJpaRepository dailyEntryRepository,
                                 GameJpaRepository gameRepository,
                                 PlayerJpaRepository playerRepository,
                                 DailyEntryPersistenceMapper mapper) {
        this.dailyEntryRepository = dailyEntryRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyEntry> loadEntries(List<Game> games, LocalDate date) {
        return dailyEntryRepository.findByGame_IdInAndEntryDate(games.stream()
                        .map(it -> it.gameId().id()).toList(), date).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyEntry> loadEntries(List<Game> games, LocalDate from, LocalDate to) {
        return dailyEntryRepository.findByGame_IdInAndEntryDateBetween(games.stream()
                        .map(it -> it.gameId().id()).toList(), from, to).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyEntry> loadEntries(List<DailyRankingKey> gamesAndDates) {
        return dailyEntryRepository.findByGame_IdInAndEntryDateIn((gamesAndDates.stream()
                        .map(it -> it.game().gameId().id()).toList()), gamesAndDates.stream()
                        .map(it -> it.date()).toList()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Batch save: inserts new entries and applies guarded corrections to existing ones. Writes are sorted by
     * natural key so that concurrent batches lock overlapping rows in the same order, which prevents deadlocks.
     */
    @Override
    @Transactional
    public List<DailyEntry> saveAll(List<DailyEntry> entries) {
        var byKey = Comparator.comparing(DailyEntry::dailyEntryKey);
        var inserts = entries.stream().filter(it -> it.dailyEntryId() == null).sorted(byKey).toList();
        var corrections = entries.stream().filter(it -> it.dailyEntryId() != null).sorted(byKey).toList();

        try {
            var written = new ArrayList<>(insert(inserts));
            corrections.forEach(this::correct);
            written.addAll(corrections);
            return written;
        } catch (ConcurrencyFailureException exception) {
            throw conflict(exception);
        } catch (DataIntegrityViolationException exception) {
            if (isUniqueViolation(exception)) {
                throw conflict(exception);
            }
            throw exception;
        }
    }

    private List<DailyEntry> insert(List<DailyEntry> entries) {
        var saved = dailyEntryRepository.saveAll(entries.stream()
                .map(entry -> new DailyEntryJpaEntity(
                        gameRepository.getReferenceById(entry.game().gameId().id()),
                        playerRepository.getReferenceById(entry.player().playerId().id()),
                        entry.date(),
                        entry.points(),
                        entry.submittedAt()))
                .toList());

        return IntStream.range(0, entries.size())
                .mapToObj(i -> withId(entries.get(i), saved.get(i).getId()))
                .toList();
    }

    private void correct(DailyEntry entry) {
        var updated = dailyEntryRepository.correctIfSubmittedEarlier(
                entry.dailyEntryId().id(), entry.points(), entry.submittedAt());
        if (updated == 0) {
            throw conflict(null);
        }
    }

    private static ConcurrentModificationConflictException conflict(Throwable cause) {
        return new ConcurrentModificationConflictException(CONFLICT_MESSAGE, cause);
    }

    private static DailyEntry withId(DailyEntry entry, Long id) {
        return new DailyEntry(new DailyEntryId(id), entry.game(), entry.date(), entry.player(), entry.points(), entry.submittedAt());
    }

    private static boolean isUniqueViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException && sqlException.getSQLState() != null) {
                return UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState());
            }
        }
        return false;
    }
}
