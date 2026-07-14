package com.pretz.geographic.infrastructure.adapter.in.api;

import com.pretz.geographic.infrastructure.adapter.in.api.dto.DailyEntryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/daily-entries")
public interface DailyEntriesApi {

    @PostMapping
    ResponseEntity<DailyEntryDto> createDailyEntry(@RequestBody DailyEntryDto dailyEntryDto);

    @PostMapping("/batch")
    ResponseEntity<List<DailyEntryDto>> createDailyEntries(@RequestBody List<DailyEntryDto> dailyEntryDtos);
}
