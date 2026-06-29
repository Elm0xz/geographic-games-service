package com.pretz.geographic.application.domain.model;

import java.util.List;

public record DailyResult(Game game, List<DailyEntry> entries) {
}
