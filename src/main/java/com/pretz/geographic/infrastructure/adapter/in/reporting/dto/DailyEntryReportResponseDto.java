package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.DailyEntry;

public record DailyEntryReportResponseDto(Long id, PlayerResponseDto player, int points) {

    public DailyEntryReportResponseDto(DailyEntry dailyEntry) {
        this(dailyEntry.dailyEntryId().id(), new PlayerResponseDto(dailyEntry.player()), dailyEntry.points());
    }
}
