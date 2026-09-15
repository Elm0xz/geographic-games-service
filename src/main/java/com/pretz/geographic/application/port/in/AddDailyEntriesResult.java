package com.pretz.geographic.application.port.in;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;
import com.pretz.geographic.application.domain.model.Player;

import java.time.LocalDate;
import java.util.List;

public record AddDailyEntriesResult(List<AddDailyEntrySuccess> successList,
                                    List<AddDailyEntryFailure> failureList) {

    public AddDailyEntriesResult() {
        this(List.of(), List.of());
    }

    public record AddDailyEntrySuccess(DailyEntry entry, DailyEntrySuccess successCode) {

        public enum DailyEntrySuccess {
            NEW,
            CORRECTED //entry timestamp was earlier than incumbent entry, becoming a new incumbent
        }
    }

    public record AddDailyEntryFailure(DailyEntryFailureKey failureKey, DailyEntryFailure failureCode) {

        public record DailyEntryFailureKey(Game game, Player player, LocalDate date) {
        }

        public enum DailyEntryFailure {
            UNKNOWN_GAME,
            UNKNOWN_PLAYER,
            INVALID_DATE,
            WEEK_CLOSED,
            SUPERSEDED //entry timestamp was equal or later than incumbent entry
        }
    }
}
