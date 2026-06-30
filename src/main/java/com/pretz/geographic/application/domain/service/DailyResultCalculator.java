package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.domain.model.DailyResult;

import java.time.LocalDate;
import java.util.List;

public interface DailyResultCalculator {

    List<DailyResult> calculateResults(List<DailyEntry> entries, LocalDate requestedDate);
}
