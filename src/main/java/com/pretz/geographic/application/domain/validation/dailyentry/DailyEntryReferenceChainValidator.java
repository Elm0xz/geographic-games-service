package com.pretz.geographic.application.domain.validation.dailyentry;

import java.util.List;

public class DailyEntryReferenceChainValidator implements DailyEntryReferenceValidator {

    private final List<DailyEntryReferenceValidator> validators;

    public DailyEntryReferenceChainValidator(List<DailyEntryReferenceValidator> validators) {
        this.validators = validators;
    }

    @Override
    public ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups) {
        ValidatedCommand result = vc;
        for (var validator : validators) {
            result = validator.validate(result, lookups);
        }
        return result;
    }
}
