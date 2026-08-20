package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto;

import com.pretz.geographic.application.domain.model.DailyEntry;

public record DailyEntryInRankingResponseDto(Long id, String player, int points) {

    public DailyEntryInRankingResponseDto(DailyEntry dailyEntry) {
        this(dailyEntry.dailyEntryId().id(), dailyEntry.player().name(), dailyEntry.points());
    }
}
