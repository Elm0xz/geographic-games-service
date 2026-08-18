package com.pretz.geographic.application.port.in;

import java.time.LocalDate;

public interface RunDailyResultCalculationsUseCase {

    void runDailyResultCalculations(LocalDate date);

    //TODO 1. fetch all daily entries from a given day
    //TODO 2. group them by game
    //TODO 3. for each game run calculations
    //TODO 4. store them in db (new table for results needed)
    //TODO 5. return result statistics (?)
}
