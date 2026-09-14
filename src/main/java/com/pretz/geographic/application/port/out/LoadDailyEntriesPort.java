package com.pretz.geographic.application.port.out;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.Game;

import java.time.LocalDate;
import java.util.List;

public interface LoadDailyEntriesPort {

    List<DailyEntry> loadEntries(List<Game> games, LocalDate date);

    List<DailyEntry> loadEntries(List<Game> games, LocalDate from, LocalDate to);
}
