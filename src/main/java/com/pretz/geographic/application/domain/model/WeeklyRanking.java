package com.pretz.geographic.application.domain.model;

import java.util.List;

public record WeeklyRanking(Game game, List<WeeklyPosition> positions) {
}
