package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.DailyEntry;

import java.util.List;

public interface SaveDailyEntryPort {

    List<DailyEntry> saveAll(List<DailyEntry> entries);
}
