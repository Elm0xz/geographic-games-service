package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryContextualValidationManager;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidationManager;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.util.List;
import java.util.stream.IntStream;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private final SaveDailyEntryPort saveDailyEntryPort;

    private final DailyEntryReferenceValidationManager referenceValidator;
    private final DailyEntryContextualValidationManager contextualValidator;
    private final DailyEntryDuplicatesResolver resolver;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort,
                               DailyEntryReferenceValidationManager referenceValidator,
                               DailyEntryContextualValidationManager contextualValidator,
                               DailyEntryDuplicatesResolver resolver) {
        this.saveDailyEntryPort = saveDailyEntryPort;
        this.referenceValidator = referenceValidator;
        this.contextualValidator = contextualValidator;
        this.resolver = resolver;
    }

    @Override
    public AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {

        var referenceValidationResult = referenceValidator.validate(addDailyEntryCommands);
        var contextualValidationResult = contextualValidator.validate(referenceValidationResult);
        var duplicateResolutionResult = resolver.resolveDuplicates(contextualValidationResult);
        var saved = saveDailyEntryPort.saveAll(getValidatedEntries(duplicateResolutionResult));

        return new AddDailyEntriesResult(toSuccessList(saved, duplicateResolutionResult), duplicateResolutionResult.failureList());
    }

    private List<DailyEntry> getValidatedEntries(AddDailyEntriesResult result) {
        return result.successList().stream().map(AddDailyEntrySuccess::entry).toList();
    }

    private List<AddDailyEntrySuccess> toSuccessList(List<DailyEntry> saved, AddDailyEntriesResult result) {
        return IntStream.range(0, saved.size())
                .mapToObj(i -> new AddDailyEntrySuccess(saved.get(i), result.successList().get(i).successCode()))
                .toList();
    }
}
