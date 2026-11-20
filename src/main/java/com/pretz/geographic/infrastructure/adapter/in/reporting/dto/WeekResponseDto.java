package com.pretz.geographic.infrastructure.adapter.in.reporting.dto;

import com.pretz.geographic.application.domain.model.Week;

public record WeekResponseDto(int year, int week) {

    public WeekResponseDto(Week week) {
        this(week.year(), week.week());
    }
}
