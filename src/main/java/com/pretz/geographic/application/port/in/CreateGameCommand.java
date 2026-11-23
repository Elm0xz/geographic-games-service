package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.ScoringSystem;

public record CreateGameCommand(String name, ScoringSystem scoringSystem) {
}
