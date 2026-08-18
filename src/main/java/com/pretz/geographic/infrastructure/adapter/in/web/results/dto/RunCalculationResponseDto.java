package com.pretz.geographic.infrastructure.adapter.in.web.results.dto;

import java.time.LocalDate;

public record RunCalculationResponseDto(LocalDate requestedDate, int gamesCalculated, int entriesDetected) {
}
