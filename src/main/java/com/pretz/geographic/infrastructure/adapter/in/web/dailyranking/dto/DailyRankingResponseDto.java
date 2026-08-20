package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto;

import com.pretz.geographic.application.domain.model.DailyRanking;

import java.time.LocalDate;
import java.util.List;

public record DailyRankingResponseDto(GameResponseDto game, LocalDate date,
                                      List<DailyEntryInRankingResponseDto> entries) {

    public DailyRankingResponseDto(DailyRanking dailyRanking) {
        this(new GameResponseDto(dailyRanking.game()), dailyRanking.date(),
                dailyRanking.entries().stream()
                        .map(DailyEntryInRankingResponseDto::new).toList());
    }
}
