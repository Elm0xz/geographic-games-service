package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.application.port.in.AddDailyEntryCommand;
import com.pretz.geographic.application.port.out.SaveDailyEntryPort;

import java.util.List;

public class DailyEntriesService implements AddDailyEntriesUseCase {

    private final SaveDailyEntryPort saveDailyEntryPort;

    public DailyEntriesService(SaveDailyEntryPort saveDailyEntryPort) {
        this.saveDailyEntryPort = saveDailyEntryPort;
    }

    @Override
    public DailyEntry addDailyEntry(AddDailyEntryCommand addDailyEntryCommand) {
        /*
        TODO 1. load game by id
        TODO 2. validate if input game name is consistent with game name in database
        TODO 3. load player by id if present; if not, load by name;
        TODO 4. if loaded by id, validate if input player name is consistent with database player name
        TODO 5. create DailyEntry domain object
        TODO 6. save DailyEntry in database
        TODO 7. return DailyEntry to controller
         */

        return null;
    }

    @Override
    public List<DailyEntry> addDailyEntries(List<AddDailyEntryCommand> addDailyEntryCommands) {
        return List.of();
    }
}
