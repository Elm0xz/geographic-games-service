package com.pretz.geographic.infrastructure.adapter.out.persistence;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.LoadDailyEntriesPort;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
class DailyEntryPersistenceAdapter implements LoadDailyEntriesPort, SaveDailyEntryPort {

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
    public List<DailyEntry> loadEntries(Game game, LocalDate from, LocalDate to) {
        return dailyEntryRepository.findByGame_NameAndEntryDateBetween(game.name(), from, to).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public DailyEntry save(DailyEntry entry) {
        var game = findOrCreateGame(entry.game());
        var player = findOrCreatePlayer(entry.player());

        var saved = dailyEntryRepository.save(
                new DailyEntryJpaEntity(game, player, entry.date(), entry.points()));

        return mapper.toDomain(saved);
    }

    private GameJpaEntity findOrCreateGame(Game game) {
        return gameRepository.findByName(game.name())
                .orElseGet(() -> gameRepository.save(
                        new GameJpaEntity(game.name(), game.scoringSystem().name())));
    }

    private PlayerJpaEntity findOrCreatePlayer(Player player) {
        return playerRepository.findByName(player.name())
                .orElseGet(() -> playerRepository.save(new PlayerJpaEntity(player.name())));
    }
}
