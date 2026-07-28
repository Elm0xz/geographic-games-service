package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import com.pretz.geographic.application.domain.model.DailyEntry;

import java.time.LocalDate;

public record DailyEntryResponseDto(Long id, GameSummaryDto game, PlayerSummaryDto player, LocalDate date, int points) {

    public DailyEntryResponseDto(DailyEntry dailyEntry) {
        this(
                dailyEntry.dailyEntryId().id(),
                new GameSummaryDto(
                        dailyEntry.game().gameId().id(),
                        dailyEntry.game().name()
                ),
                new PlayerSummaryDto(
                        dailyEntry.player().playerId().id(),
                        dailyEntry.player().name()
                ),
                dailyEntry.date(),
                dailyEntry.points()
        );
    }
}
