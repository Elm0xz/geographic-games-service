package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.SavePlayerPort;
import com.pretz.geographic.application.port.out.exception.PlayerAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pretz.geographic.infrastructure.adapter.out.persistence.UniqueViolations.isUniqueViolationOf;

@Component
public class PlayerPersistenceAdapter implements LoadPlayerPort, SavePlayerPort {

    private static final String UNIQUE_NAME_CONSTRAINT = "uq_player_name";

    private final PlayerJpaRepository playerJpaRepository;
    private final PlayerPersistenceMapper playerPersistenceMapper;

    public PlayerPersistenceAdapter(PlayerJpaRepository playerJpaRepository,
                                    PlayerPersistenceMapper playerPersistenceMapper) {
        this.playerJpaRepository = playerJpaRepository;
        this.playerPersistenceMapper = playerPersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> loadAllPlayers() {
        return playerJpaRepository.findAll()
                .stream()
                .map(playerPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> loadPlayers(List<Long> ids) {

        return playerJpaRepository.findAllById(ids)
                .stream()
                .map(playerPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Player save(Player player) {
        try {
            return playerPersistenceMapper.toDomain(playerJpaRepository.saveAndFlush(new PlayerJpaEntity(player.name())));
        } catch (DataIntegrityViolationException exception) {
            if (isUniqueViolationOf(exception, UNIQUE_NAME_CONSTRAINT)) {
                throw new PlayerAlreadyExistsException(player.name(), exception);
            }
            throw exception;
        }
    }
}
