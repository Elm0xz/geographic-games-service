package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Player;

public interface LoadPlayerPort {

    Player loadPlayer(Long id);
}
