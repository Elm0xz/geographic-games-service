package com.pretz.geographic.infrastructure.adapter.in.api;

import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.infrastructure.adapter.in.api.dto.CreateDailyEntryRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.api.dto.DailyEntryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DailyEntriesController implements DailyEntriesApi {

    private final AddDailyEntriesUseCase addDailyEntriesUseCase;

    public DailyEntriesController(AddDailyEntriesUseCase addDailyEntriesUseCase) {
        this.addDailyEntriesUseCase = addDailyEntriesUseCase;
    }

    @Override
    public ResponseEntity<DailyEntryResponseDto> createDailyEntry(CreateDailyEntryRequestDto createDailyEntryRequestDto) {
        var result = addDailyEntriesUseCase.addDailyEntry(createDailyEntryRequestDto.toCommand());
        return ResponseEntity.ok(new DailyEntryResponseDto(result));
    }

    @Override
    public ResponseEntity<List<DailyEntryResponseDto>> createDailyEntries(List<CreateDailyEntryRequestDto> createDailyEntryRequestDtos) {
        var result = addDailyEntriesUseCase.addDailyEntries(createDailyEntryRequestDtos.stream()
                .map(CreateDailyEntryRequestDto::toCommand).toList());
        return ResponseEntity.ok(result.stream().map(DailyEntryResponseDto::new).toList());
    }
}
