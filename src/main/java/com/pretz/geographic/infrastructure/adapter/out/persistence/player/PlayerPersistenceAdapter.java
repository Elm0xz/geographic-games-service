package com.pretz.geographic.infrastructure.adapter.out.persistence.player;

import com.pretz.geographic.application.domain.model.Player;
import com.pretz.geographic.application.port.out.LoadPlayerPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<Player> loadPlayers(List<Long> ids) {

        return playerJpaRepository.findAllById(ids)
                .stream()
                .map(playerPersistenceMapper::toDomain)
                .toList();
    }
}
