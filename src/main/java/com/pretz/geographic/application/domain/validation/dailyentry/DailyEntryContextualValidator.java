package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.domain.service.ValidationIntermediateResult;

public interface DailyEntryContextualValidator {

    ValidationIntermediateResult validate(ValidationIntermediateResult validationIntermediateResult,
                                          ContextualLookups contextualLookups);
}
