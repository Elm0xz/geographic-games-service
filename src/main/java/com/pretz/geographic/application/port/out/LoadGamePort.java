package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.Game;

public interface LoadGamePort {

    Game loadGame(Long id);
}
