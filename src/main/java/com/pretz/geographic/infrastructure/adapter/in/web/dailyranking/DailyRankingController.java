package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking;

import com.pretz.geographic.application.port.in.GetDailyRankingUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto.DailyRankingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DailyRankingController implements DailyRankingApi {

    private final GetDailyRankingUseCase getDailyRankingUseCase;

    public DailyRankingController(GetDailyRankingUseCase getDailyRankingUseCase) {
        this.getDailyRankingUseCase = getDailyRankingUseCase;
    }

    @Override
    public ResponseEntity<List<DailyRankingResponseDto>> getDailyRankings(LocalDate date) {
        return ResponseEntity.ok(getDailyRankingUseCase.getDailyRankings(date).stream()
                .map(DailyRankingResponseDto::new).toList());
    }
}
