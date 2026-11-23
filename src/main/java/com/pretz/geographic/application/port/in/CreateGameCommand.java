package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.ScoringSystem;

/**
 * @param scoringSystem defaults to {@link ScoringSystem#STANDARD} when {@code null}
 */
public record CreateGameCommand(String name, ScoringSystem scoringSystem) {

    public CreateGameCommand {
        if (scoringSystem == null) {
            scoringSystem = ScoringSystem.STANDARD;
        }
    }
}
