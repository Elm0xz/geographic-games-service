package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking;

import com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.dto.DailyRankingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/daily-ranking")
public interface DailyRankingApi {

    @GetMapping
    ResponseEntity<List<DailyRankingResponseDto>> getDailyRankings(@RequestParam LocalDate date);

    //TODO single game rankings
}
