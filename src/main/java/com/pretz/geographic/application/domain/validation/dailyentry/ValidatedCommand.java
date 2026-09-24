package com.pretz.geographic.application.domain.validation.dailyentry;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;

import java.util.List;

public record ValidatedCommand(AddDailyEntryCommand command,
                               List<AddDailyEntryFailure.Reason> rejectionResults) {
}
