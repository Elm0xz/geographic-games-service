package com.pretz.geographic.application.port.in.dailyentry;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;

import java.util.List;

public interface AddDailyEntriesUseCase {

    DailyEntry addDailyEntry(AddDailyEntryCommand addDailyEntryCommand);

    AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands);
}
