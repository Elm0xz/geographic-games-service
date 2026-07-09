package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.DailyEntry;

public interface SaveDailyEntryPort {

    /**
     * Persists a single daily entry, creating the referenced game and player on demand,
     * and returns the stored entry.
     */
    DailyEntry save(DailyEntry entry);
}
