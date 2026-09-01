package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking;

import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto.FullWeeklyRankingsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
public class WeeklyRankingController implements WeeklyRankingApi {

    private final GetWeeklyRankingUseCase getWeeklyRankingUseCase;

    public WeeklyRankingController(GetWeeklyRankingUseCase getWeeklyRankingUseCase) {
        this.getWeeklyRankingUseCase = getWeeklyRankingUseCase;
    }

    @Override
    public ResponseEntity<FullWeeklyRankingsResponseDto> getWeeklyRankings(Year year, int week) {
        return ResponseEntity.ok(new FullWeeklyRankingsResponseDto(
                getWeeklyRankingUseCase.getWeeklyRankings(new Week(year.getValue(), week)))); //TODO ISO mapping of weeks
    }

    //TODO unified exception handlers in different task
}
