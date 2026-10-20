package com.pretz.geographic.application.port.in.dailyentry.result;

import com.pretz.geographic.application.domain.model.DailyEntry;

public record AddDailyEntrySuccess(DailyEntry entry, DailyEntrySuccess successCode) {

    public enum DailyEntrySuccess {
        NEW, //no entry was persisted before, this entry is now persisted
        CORRECTED //an entry was already persisted, this one replaced it because it was submitted earlier
    }
}
