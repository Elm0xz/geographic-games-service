package com.pretz.geographic.infrastructure.adapter.in.api.dto;

import com.pretz.geographic.application.domain.model.DailyEntry;

//TODO implement
public record DailyEntryResponseDto() {

    public DailyEntryResponseDto(DailyEntry addDailyEntryCommand) {
        this();
    }
}
