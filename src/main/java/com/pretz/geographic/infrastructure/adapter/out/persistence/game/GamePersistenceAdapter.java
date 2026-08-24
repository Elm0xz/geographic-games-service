package com.pretz.geographic.infrastructure.adapter.out.persistence.game;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.port.out.LoadGamePort;
import com.pretz.geographic.application.port.out.exception.GameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GamePersistenceAdapter implements LoadGamePort {

    private final GameJpaRepository gameJpaRepository;
    private final GamePersistenceMapper gamePersistenceMapper;

    public GamePersistenceAdapter(GameJpaRepository gameJpaRepository,
                                  GamePersistenceMapper gamePersistenceMapper) {
        this.gameJpaRepository = gameJpaRepository;
        this.gamePersistenceMapper = gamePersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Game loadGame(Long id) {
        return gameJpaRepository.findById(id)
                .map(gamePersistenceMapper::toDomain)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    //for now, we assume all games are active; in the future we will add a boolean activity status field in schema
    @Override
    @Transactional(readOnly = true)
    public List<Game> loadActiveGames() {
        return gameJpaRepository.findAll()
                .stream()
                .map(gamePersistenceMapper::toDomain)
                .toList();
    }
}
