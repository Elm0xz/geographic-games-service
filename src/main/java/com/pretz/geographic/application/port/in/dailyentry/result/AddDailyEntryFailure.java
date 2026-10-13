package com.pretz.geographic.application.port.in.dailyentry.result;

import com.pretz.geographic.application.port.in.dailyentry.AddDailyEntryCommand;

import java.util.List;

public record AddDailyEntryFailure(AddDailyEntryCommand failedCommand, List<Reason> reasons) {

    public enum Reason {
        UNKNOWN_GAME,
        UNKNOWN_PLAYER,
        INVALID_DATE,
        WEEK_CLOSED,
        SUPERSEDED //entry timestamp was equal or later than incumbent entry
    }
}

