package com.pretz.geographic.application.domain.service;

import com.pretz.geographic.application.domain.model.DailyEntry;
import com.pretz.geographic.application.port.in.dailyentry.result.AddDailyEntryFailure;

import java.util.List;

public record ValidationIntermediateResult(List<DailyEntry> entriesPassed,
                                           List<AddDailyEntryFailure> failures) {
}
