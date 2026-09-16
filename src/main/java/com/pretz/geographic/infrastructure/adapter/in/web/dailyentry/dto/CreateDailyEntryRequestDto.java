package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.time.LocalDate;

public record CreateDailyEntryRequestDto(
        @NotNull @Valid GameRefDto game,
        @NotNull @Valid PlayerRefDto player,
        @NotNull  LocalDate date,
        @PositiveOrZero int points,
        @NotNull Instant submittedAt) {

    public AddDailyEntryCommand toCommand() {
        return new AddDailyEntryCommand(
                new AddDailyEntryCommand.GameRef(game.id(), game.name()),
                new AddDailyEntryCommand.PlayerRef(player.id(), player.name()),
                date,
                points,
                submittedAt
        );
    }
}
