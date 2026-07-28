package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GameRefDto(
        @NotNull @Positive Long id,
        @NotBlank String name) {
}
