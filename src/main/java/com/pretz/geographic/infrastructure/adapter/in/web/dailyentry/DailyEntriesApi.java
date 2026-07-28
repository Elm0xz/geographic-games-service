package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntryRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.DailyEntryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/daily-entries")
public interface DailyEntriesApi {

    @PostMapping
    ResponseEntity<DailyEntryResponseDto> createDailyEntry(@Valid @RequestBody CreateDailyEntryRequestDto createDailyEntryRequestDto);

    @PostMapping("/batch")
    ResponseEntity<List<DailyEntryResponseDto>> createDailyEntries(
            @NotEmpty @Valid @RequestBody List<@Valid CreateDailyEntryRequestDto> createDailyEntryRequestDtos);
}
