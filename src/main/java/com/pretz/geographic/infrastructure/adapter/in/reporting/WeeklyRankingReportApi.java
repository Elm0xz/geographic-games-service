package com.pretz.geographic.infrastructure.adapter.in.reporting;

import com.pretz.geographic.infrastructure.adapter.in.reporting.dto.WeeklyRankingsReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;

@RequestMapping("/api/reports/weekly-rankings")
public interface WeeklyRankingReportApi {

    /**
     * Returns weekly rankings of all games for ISO weeks between {@code fromYear-W fromWeek} and
     * {@code toYear-W toWeek}, both inclusive, sorted by week and game name. Weeks without a calculated ranking are
     * omitted. Responds with {@code 400 INVALID_DATE} when a week doesn't exist, the start week is after the end week
     * or the end week is not in the past.
     */
    @GetMapping
    ResponseEntity<WeeklyRankingsReportResponseDto> getWeeklyRankings(@RequestParam Year fromYear,
                                                                      @RequestParam int fromWeek,
                                                                      @RequestParam Year toYear,
                                                                      @RequestParam int toWeek);

    /**
     * Returns weekly rankings of a single game in the week interval, sorted by week. Behaves like
     * {@link #getWeeklyRankings(Year, int, Year, int)} and additionally responds with {@code 404 NOT_FOUND} when
     * the game doesn't exist.
     */
    @GetMapping("/games/{gameId}")
    ResponseEntity<WeeklyRankingsReportResponseDto> getWeeklyRankings(@PathVariable Long gameId,
                                                                      @RequestParam Year fromYear,
                                                                      @RequestParam int fromWeek,
                                                                      @RequestParam Year toYear,
                                                                      @RequestParam int toWeek);
}
