package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.service.ValidationIntermediateResult;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;

import java.util.Set;

public class DailyEntryContextualValidationManager {

    private final LoadWeeklyRankingPort loadWeeklyRankingPort;
    private final DailyEntryContextualValidator validator;

    public DailyEntryContextualValidationManager(LoadWeeklyRankingPort loadWeeklyRankingPort, DailyEntryContextualValidator validator) {
        this.loadWeeklyRankingPort = loadWeeklyRankingPort;
        this.validator = validator;
    }

    public ValidationIntermediateResult validate(ValidationIntermediateResult intermediateResult) {
        Set<GameWeek> gameWeeks = loadWeeklyRankingPort.loadCalculatedWeeks(intermediateResult.entriesPassed().stream()
                .map(it -> new GameWeek(it.game().gameId(), it.getWeek())).toList());
        var contextualLookup = new ContextualLookups(gameWeeks);

        return validator.validate(intermediateResult, contextualLookup);
    }
}
