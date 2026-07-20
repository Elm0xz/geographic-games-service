package com.pretz.geographic.application.port.in;

import java.time.LocalDate;

public record AddDailyEntryCommand(Long gameId, String gameName,
                                   Long playerId, String playerName,
                                   LocalDate date, int points) {
}
