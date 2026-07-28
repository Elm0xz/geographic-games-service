package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PlayerRefDto(
        @Positive Long id,
        @NotBlank String name) {
}
