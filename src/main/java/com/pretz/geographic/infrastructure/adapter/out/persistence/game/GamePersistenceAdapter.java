package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.SaveGamePort;
import com.pretz.geographic.application.port.out.exception.GameAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pretz.geographic.infrastructure.adapter.out.persistence.UniqueViolations.isUniqueViolationOf;

@Component
public class GamePersistenceAdapter implements LoadGamePort, SaveGamePort {

    private static final String UNIQUE_NAME_CONSTRAINT = "uq_game_name";

    private final GameJpaRepository gameJpaRepository;
    private final GamePersistenceMapper gamePersistenceMapper;

    public GamePersistenceAdapter(GameJpaRepository gameJpaRepository,
                                  GamePersistenceMapper gamePersistenceMapper) {
        this.gameJpaRepository = gameJpaRepository;
        this.gamePersistenceMapper = gamePersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> loadAllGames() {
        return gameJpaRepository.findAll()
                .stream()
                .map(gamePersistenceMapper::toDomain)
                .toList();
    }

    //for now, we assume all games are active; soon we will add a boolean activity status field in schema
    @Override
    @Transactional(readOnly = true)
    public List<Game> loadActiveGames() {
        return gameJpaRepository.findAll()
                .stream()
                .map(gamePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> loadGames(List<Long> gameIds) {
        return gameJpaRepository.findAllById(gameIds)
                .stream()
                .map(gamePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Game save(Game game) {
        try {
            return gamePersistenceMapper.toDomain(gameJpaRepository.saveAndFlush(new GameJpaEntity(game.name(), game.scoringSystem())));
        } catch (DataIntegrityViolationException exception) {
            if (isUniqueViolationOf(exception, UNIQUE_NAME_CONSTRAINT)) {
                throw new GameAlreadyExistsException(game.name(), exception);
            }
            throw exception;
        }
    }
}
