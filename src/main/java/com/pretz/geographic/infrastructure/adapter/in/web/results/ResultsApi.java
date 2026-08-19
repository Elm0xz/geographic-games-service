package com.pretz.geographic.infrastructure.adapter.in.web.results;

import com.pretz.geographic.infrastructure.adapter.in.web.results.dto.RunCalculationResponseDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@RequestMapping("/api/results")
public interface ResultsApi {

    @PostMapping
    ResponseEntity<RunCalculationResponseDto> runDailyCalculation(@NotNull @PastOrPresent @RequestParam LocalDate date);

}
