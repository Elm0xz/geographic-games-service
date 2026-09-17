package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateDailyEntriesRequestDto(@NotNull @NotEmpty List<@Valid CreateDailyEntryRequestDto> createDailyEntryRequestDtos) {
}
