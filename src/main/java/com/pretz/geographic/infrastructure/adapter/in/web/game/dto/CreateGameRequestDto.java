package com.pretz.geographic.infrastructure.adapter.in.web.game.dto;

import com.pretz.geographic.application.domain.model.ScoringSystem;
import com.pretz.geographic.application.port.in.CreateGameCommand;
import com.pretz.geographic.infrastructure.adapter.in.web.NameConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateGameRequestDto(
        @NotBlank @Size(max = NameConstraints.MAX_LENGTH) @Pattern(regexp = NameConstraints.PATTERN) String name,
        @NotNull ScoringSystem scoringSystem) {

    public CreateGameCommand toCommand() {
        return new CreateGameCommand(name, scoringSystem);
    }
}
