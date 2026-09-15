package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesResponseDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntryRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/daily-entries")
public interface DailyEntriesApi {

    @PostMapping
    ResponseEntity<CreateDailyEntryResponseDto> createDailyEntry(@Valid @RequestBody CreateDailyEntryRequestDto createDailyEntryRequestDto);

    @PostMapping("/batch")
    ResponseEntity<CreateDailyEntriesResponseDto> createDailyEntries(
            @NotEmpty @Valid @RequestBody CreateDailyEntriesRequestDto createDailyEntriesRequestDto);
}
