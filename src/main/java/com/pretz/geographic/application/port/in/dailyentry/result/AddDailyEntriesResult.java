package com.pretz.geographic.application.port.in.dailyentry.result;

import java.util.List;

public record AddDailyEntriesResult(List<AddDailyEntrySuccess> successList,
                                    List<AddDailyEntryFailure> failureList) {

    public AddDailyEntriesResult() {
        this(List.of(), List.of());
    }
}
