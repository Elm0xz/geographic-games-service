package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.List;

public interface LoadDailyEntriesPort {

    /**
     * Loads all daily entries for the given game within the inclusive date range.
     * <p>
     * A date range covers both the daily calculator (a single day, {@code from == to})
     * and the weekly calculator (the seven days of a week), keeping this port free of
     * week arithmetic.
     */
    List<DailyEntry> loadEntries(Game game, LocalDate from, LocalDate to);
}
