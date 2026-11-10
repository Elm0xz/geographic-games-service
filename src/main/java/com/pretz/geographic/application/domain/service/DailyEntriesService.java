package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryContextualValidationManager;
import com.pretz.geographic.application.domain.validation.dailyentry.DailyEntryReferenceValidationManager;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntrySuccess;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private static final Logger log = LoggerFactory.getLogger(DailyEntriesService.class);

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

        logRejections(duplicateResolutionResult.failureList());

        return new AddDailyEntriesResult(toSuccessList(saved, duplicateResolutionResult.successList()), duplicateResolutionResult.failureList());
    }

    private List<DailyEntry> getValidatedEntries(AddDailyEntriesResult result) {
        return result.successList().stream().map(AddDailyEntrySuccess::entry).toList();
    }

    private List<AddDailyEntrySuccess> toSuccessList(List<DailyEntry> saved, List<AddDailyEntrySuccess> successList) {

        var successCodes = successList.stream()
                .collect(Collectors.toMap(it -> it.entry().dailyEntryKey(), AddDailyEntrySuccess::successCode));

        return saved.stream()
                .map(it -> new AddDailyEntrySuccess(it, successCodes.get(it.dailyEntryKey())))
                .toList();
    }

    private void logRejections(List<AddDailyEntryFailure> failureList) {
        failureList.forEach(failure -> {
            var command = failure.failedCommand();
            log.info("Daily entry rejected: game={}, player={}, date={}, reasons={}",
                    command.game(), command.player(), command.date(), failure.reasons());
        });
    }
}
