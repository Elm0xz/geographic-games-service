package com.pretz.geographic.application.port.in;

import java.time.LocalDate;

public record DailyResultStatistics(LocalDate date, int gamesCalculated, int entriesDetected) {

}
