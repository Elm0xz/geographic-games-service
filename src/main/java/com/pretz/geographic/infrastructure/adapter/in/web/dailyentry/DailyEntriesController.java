package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.port.in.AddDailyEntriesUseCase;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntriesResponseDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntryRequestDto;
import com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto.CreateDailyEntryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class DailyEntriesController implements DailyEntriesApi {

    private final AddDailyEntriesUseCase addDailyEntriesUseCase;

    public DailyEntriesController(AddDailyEntriesUseCase addDailyEntriesUseCase) {
        this.addDailyEntriesUseCase = addDailyEntriesUseCase;
    }

    @Override
    public ResponseEntity<CreateDailyEntryResponseDto> createDailyEntry(CreateDailyEntryRequestDto createDailyEntryRequestDto) {
        var result = addDailyEntriesUseCase.addDailyEntry(createDailyEntryRequestDto.toCommand());
        return ResponseEntity.status(CREATED).body(new CreateDailyEntryResponseDto(result));
    }

    @Override
    public ResponseEntity<CreateDailyEntriesResponseDto> createDailyEntries(CreateDailyEntriesRequestDto createDailyEntriesRequestDto) {
        var result = addDailyEntriesUseCase.addDailyEntries(createDailyEntriesRequestDto.createDailyEntryRequestDtos().stream()
                .map(CreateDailyEntryRequestDto::toCommand).toList());
        return ResponseEntity.status(CREATED).body(new CreateDailyEntriesResponseDto(result));
    }
}
