package com.pretz.geographic.infrastructure.adapter.in.api;

import com.pretz.geographic.infrastructure.adapter.in.api.dto.CreateDailyEntryRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.api.dto.DailyEntryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/daily-entries")
public interface DailyEntriesApi {

    @PostMapping
    ResponseEntity<DailyEntryResponseDto> createDailyEntry(@RequestBody CreateDailyEntryRequestDto createDailyEntryRequestDto);

    @PostMapping("/batch")
    ResponseEntity<List<DailyEntryResponseDto>> createDailyEntries(@RequestBody List<CreateDailyEntryRequestDto> createDailyEntryRequestDtos);
}
