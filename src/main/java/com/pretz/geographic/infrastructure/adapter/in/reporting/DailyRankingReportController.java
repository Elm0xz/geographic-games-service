package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.application.domain.model.GameId;
import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.infrastructure.adapter.in.reporting.dto.DailyRankingsReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DailyRankingReportController implements DailyRankingReportApi {

    private final GetDailyRankingUseCase getDailyRankingUseCase;

    public DailyRankingReportController(GetDailyRankingUseCase getDailyRankingUseCase) {
        this.getDailyRankingUseCase = getDailyRankingUseCase;
    }

    @Override
    public ResponseEntity<DailyRankingsReportResponseDto> getDailyRankings(LocalDate from, LocalDate to) {
        return ResponseEntity.ok(new DailyRankingsReportResponseDto(from, to,
                getDailyRankingUseCase.getDailyRankings(from, to)));
    }

    @Override
    public ResponseEntity<DailyRankingsReportResponseDto> getDailyRankings(Long gameId, LocalDate from, LocalDate to) {
        return ResponseEntity.ok(new DailyRankingsReportResponseDto(from, to,
                getDailyRankingUseCase.getDailyRankings(from, to, new GameId(gameId))));
    }
}
