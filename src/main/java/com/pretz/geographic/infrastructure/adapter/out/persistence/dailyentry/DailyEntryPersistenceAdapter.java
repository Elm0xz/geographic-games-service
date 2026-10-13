package com.pretz.geographic.infrastructure.adapter.out.persistence.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.service.DailyRankingService;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import com.pretz.geographic.infrastructure.adapter.out.persistence.game.GameJpaRepository;
import com.pretz.geographic.infrastructure.adapter.out.persistence.player.PlayerJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class DailyEntryPersistenceAdapter implements LoadDailyEntriesPort, SaveDailyEntryPort {

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
    public List<DailyEntry> loadEntries(List<DailyRankingService.GameAndDate> gamesAndDates) {
        return dailyEntryRepository.findByGame_IdInAndEntryDateIn((gamesAndDates.stream()
                        .map(it -> it.game().gameId().id()).toList()), gamesAndDates.stream()
                        .map(it -> it.date()).toList()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public DailyEntry save(DailyEntry entry) {

        var game = gameRepository.getReferenceById(entry.game().gameId().id());
        var player = playerRepository.getReferenceById(entry.player().playerId().id());

        var saved = dailyEntryRepository.save(
                new DailyEntryJpaEntity(game, player, entry.date(), entry.points(), entry.submittedAt()));

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<DailyEntry> saveAll(List<DailyEntry> entries) {
        var jpaEntities = entries.stream()
                .map(entry -> new DailyEntryJpaEntity(
                        entry.dailyEntryId() != null ? entry.dailyEntryId().id() : null,
                        gameRepository.getReferenceById(entry.game().gameId().id()),
                        playerRepository.getReferenceById(entry.player().playerId().id()),
                        entry.date(),
                        entry.points(),
                        entry.submittedAt()))
                .toList();

        return dailyEntryRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
