package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import com.pretz.geographic.application.port.out.exception.PlayerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class PlayerPersistenceAdapter implements LoadPlayerPort {

    private final PlayerJpaRepository playerJpaRepository;
    private final PlayerPersistenceMapper playerPersistenceMapper;

    public PlayerPersistenceAdapter(PlayerJpaRepository playerJpaRepository,
                                    PlayerPersistenceMapper playerPersistenceMapper) {
        this.playerJpaRepository = playerJpaRepository;
        this.playerPersistenceMapper = playerPersistenceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Player loadPlayer(Long id) {
        return playerJpaRepository.findById(id)
                .map(playerPersistenceMapper::toDomain)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Player loadPlayer(String name) {
        return playerJpaRepository.findByName(name)
                .map(playerPersistenceMapper::toDomain)
                .orElseThrow(() -> new PlayerNotFoundException(name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> loadPlayers(List<Long> ids) {

        return playerJpaRepository.findAllById(ids)
                .stream()
                .map(playerPersistenceMapper::toDomain)
                .toList();
    }
}
