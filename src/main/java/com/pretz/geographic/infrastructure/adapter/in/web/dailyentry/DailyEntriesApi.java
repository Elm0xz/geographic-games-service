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

    /**
     * Submits a batch of daily entries. The batch is saved all-or-nothing.
     *
     * <h2>Responses</h2>
     * <ul>
     *     <li><b>200 OK</b>: the batch was processed. The body lists accepted entries ({@code NEW} or {@code CORRECTED})
     *     and rejected entries with their reasons. A batch where every entry was rejected still returns 200.</li>
     *     <li><b>400 Bad Request</b> ({@code VALIDATION_ERROR}, {@code MALFORMED_REQUEST}): the request is
     *     structurally invalid. Nothing was saved. Fix the request; do not retry it unchanged.</li>
     *     <li><b>409 Conflict</b> ({@code CONCURRENT_MODIFICATION}): another request wrote some of the same
     *     {@code (game, player, date)} keys at the same time. Nothing from this batch was saved. Retry the same batch.</li>
     * </ul>
     */
    @PostMapping
    ResponseEntity<CreateDailyEntriesResponseDto> createDailyEntries(@Valid @RequestBody CreateDailyEntriesRequestDto createDailyEntriesRequestDto);
}
