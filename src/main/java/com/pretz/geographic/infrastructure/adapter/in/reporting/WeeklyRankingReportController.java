package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.domain.model.Week;
import com.pretz.geographic.application.port.in.GetWeeklyRankingUseCase;
import com.pretz.geographic.infrastructure.adapter.in.reporting.dto.WeeklyRankingsReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
public class WeeklyRankingReportController implements WeeklyRankingReportApi {

    private final GetWeeklyRankingUseCase getWeeklyRankingUseCase;

    public WeeklyRankingReportController(GetWeeklyRankingUseCase getWeeklyRankingUseCase) {
        this.getWeeklyRankingUseCase = getWeeklyRankingUseCase;
    }

    @Override
    public ResponseEntity<WeeklyRankingsReportResponseDto> getWeeklyRankings(Year fromYear, int fromWeek,
                                                                             Year toYear, int toWeek) {
        var from = Week.of(fromYear.getValue(), fromWeek);
        var to = Week.of(toYear.getValue(), toWeek);
        return ResponseEntity.ok(new WeeklyRankingsReportResponseDto(from, to,
                getWeeklyRankingUseCase.getWeeklyRankings(from, to)));
    }

    @Override
    public ResponseEntity<WeeklyRankingsReportResponseDto> getWeeklyRankings(Long gameId,
                                                                             Year fromYear, int fromWeek,
                                                                             Year toYear, int toWeek) {
        var from = Week.of(fromYear.getValue(), fromWeek);
        var to = Week.of(toYear.getValue(), toWeek);
        return ResponseEntity.ok(new WeeklyRankingsReportResponseDto(from, to,
                getWeeklyRankingUseCase.getWeeklyRankings(from, to, new GameId(gameId))));
    }
}
