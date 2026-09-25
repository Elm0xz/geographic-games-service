package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.model.GameWeek;
import com.pretz.geographic.application.domain.service.ValidationIntermediateResult;
import com.pretz.geographic.application.port.out.LoadWeeklyRankingPort;

public class DailyEntryContextualValidationManager {

    private final LoadWeeklyRankingPort loadWeeklyRankingPort;
    private final DailyEntryContextualValidator validator;

    public DailyEntryContextualValidationManager(LoadWeeklyRankingPort loadWeeklyRankingPort, DailyEntryContextualValidator validator) {
        this.loadWeeklyRankingPort = loadWeeklyRankingPort;
        this.validator = validator;
    }

    public ValidationIntermediateResult validate(ValidationIntermediateResult intermediateResult) {
        var contextualLookup = new ContextualLookups(loadWeeklyRankingPort.loadCalculatedWeeks(intermediateResult.entriesPassed().stream()
                .map(it -> new GameWeek(it.game().gameId(), it.getWeek())).toList()));

        return validator.validate(intermediateResult, contextualLookup);
    }
}
