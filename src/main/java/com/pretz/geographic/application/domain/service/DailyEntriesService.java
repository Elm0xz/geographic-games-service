package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.AddDailyEntryCommand;

import java.util.List;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    @Override
    public DailyEntry addDailyEntry(AddDailyEntryCommand addDailyEntryCommand) {
        return null;
    }

    @Override
    public List<DailyEntry> addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {
        return List.of();
    }
}
