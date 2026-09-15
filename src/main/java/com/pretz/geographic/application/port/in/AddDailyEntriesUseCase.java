package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.DailyEntry;

import java.util.List;

public interface AddDailyEntriesUseCase {

    DailyEntry addDailyEntry(AddDailyEntryCommand addDailyEntryCommand);

    AddDailyEntriesResult addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands);
}
