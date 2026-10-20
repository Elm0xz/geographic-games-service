package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/daily-entries")
public interface DailyEntriesApi {

    @PostMapping
    ResponseEntity<CreateDailyEntriesResponseDto> createDailyEntries(@Valid @RequestBody CreateDailyEntriesRequestDto createDailyEntriesRequestDto);
}
