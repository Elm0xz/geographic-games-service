package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import com.pretz.geographic.application.port.in.AddDailyEntryCommand;

import java.time.LocalDate;

public record CreateDailyEntryRequestDto(GameRefDto game, PlayerRefDto player, LocalDate date, int points) {

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
