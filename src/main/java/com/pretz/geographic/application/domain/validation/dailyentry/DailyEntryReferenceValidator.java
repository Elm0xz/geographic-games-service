package com.pretz.geographic.application.domain.validation.dailyentry;

public interface DailyEntryReferenceValidator {

    ValidatedCommand validate(ValidatedCommand vc, ReferenceLookups lookups);
}
