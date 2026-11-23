package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto;

import com.pretz.geographic.application.domain.model.DailyEntry;

public record DailyEntryInRankingResponseDto(Long id, PlayerResponseDto player, int points) {

    public DailyEntryInRankingResponseDto(DailyEntry dailyEntry) {
        this(dailyEntry.dailyEntryId().id(), new PlayerResponseDto(dailyEntry.player()), dailyEntry.points());
    }
}
