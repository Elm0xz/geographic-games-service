package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;

import java.util.Map;

public record ReferenceLookups(Map<Long, Game> games, Map<Long, Player> players) {
}
