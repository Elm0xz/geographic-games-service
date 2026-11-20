package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.infrastructure.adapter.in.reporting.dto.DailyRankingsReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@RequestMapping("/api/reports/daily-rankings")
public interface DailyRankingReportApi {

    /**
     * Returns daily rankings of all active games for every day in the interval {@code [from, to]}, sorted by game name
     * and date. Days on which a game has no results are omitted. Responds with {@code 400 INVALID_DATE} when
     * {@code from} is after {@code to} or {@code to} is not in the past.
     */
    @GetMapping
    ResponseEntity<DailyRankingsReportResponseDto> getDailyRankings(@RequestParam LocalDate from,
                                                                    @RequestParam LocalDate to);

    /**
     * Returns daily rankings of a single game for every day in the interval {@code [from, to]}, sorted by date.
     * Days without results are omitted. Responds with {@code 404 NOT_FOUND} when the game doesn't exist and with
     * {@code 400 INVALID_DATE} when {@code from} is after {@code to} or {@code to} is not in the past.
     */
    @GetMapping("/games/{gameId}")
    ResponseEntity<DailyRankingsReportResponseDto> getDailyRankings(@PathVariable Long gameId,
                                                                    @RequestParam LocalDate from,
                                                                    @RequestParam LocalDate to);
}
