package com.pretz.geographic.application.port.in.dailyentry.result;

import com.pretz.geographic.application.domain.model.DailyEntry;

public record AddDailyEntrySuccess(DailyEntry entry, DailyEntrySuccess successCode) {

    public enum DailyEntrySuccess {
        NEW,
        CORRECTED //entry timestamp was earlier than incumbent entry, becoming a new incumbent
    }
}
