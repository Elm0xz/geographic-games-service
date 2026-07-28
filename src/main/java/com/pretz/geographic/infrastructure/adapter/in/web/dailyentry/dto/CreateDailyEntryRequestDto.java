package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import com.pretz.geographic.application.port.in.AddDailyEntryCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record CreateDailyEntryRequestDto(
        @NotNull @Valid GameRefDto game,
        @NotNull @Valid PlayerRefDto player,
        @NotNull @PastOrPresent LocalDate date,
        @PositiveOrZero int points) {

    public AddDailyEntryCommand toCommand() {
        return new AddDailyEntryCommand(
                game.id(),
                game.name(),
                player.id(),
                player.name(),
                date,
                points
        );
    }
}
