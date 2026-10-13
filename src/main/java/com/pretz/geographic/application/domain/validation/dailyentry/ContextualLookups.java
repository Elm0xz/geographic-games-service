package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.GameWeek;

import java.util.Set;

public record ContextualLookups(Set<GameWeek> gameWeeks) {
}
