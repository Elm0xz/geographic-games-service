package com.pretz.geographic.infrastructure.adapter.in.web.player.dto;

import com.pretz.geographic.application.port.in.CreatePlayerCommand;
import com.pretz.geographic.infrastructure.adapter.in.web.NameConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequestDto(
        @NotBlank @Size(max = NameConstraints.MAX_LENGTH) @Pattern(regexp = NameConstraints.PATTERN) String name) {

    public CreatePlayerCommand toCommand() {
        return new CreatePlayerCommand(name);
    }
}
