package com.pretz.geographic.application.port.in.dailyentry;

import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntriesResult;

import java.util.List;

public interface AddDailyEntriesUseCase {

    AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands);
}
