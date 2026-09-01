package com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking;

import com.pretz.geographic.infrastructure.adapter.in.web.weeklyranking.dto.FullWeeklyRankingsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;

@RequestMapping("/api/weekly-ranking")
public interface WeeklyRankingApi {

    @GetMapping
    ResponseEntity<FullWeeklyRankingsResponseDto> getWeeklyRankings(@RequestParam Year year, @RequestParam int week); //TODO validate week number valid

    //TODO single game weekly rankings
}

