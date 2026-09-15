
package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.dto;

import com.pretz.geographic.application.port.in.AddDailyEntriesResult;
import com.pretz.geographic.application.port.in.AddDailyEntriesResult.AddDailyEntryFailure;
import com.pretz.geographic.application.port.in.AddDailyEntriesResult.AddDailyEntrySuccess;

import java.time.LocalDate;
import java.util.List;

public record CreateDailyEntriesResponseDto(
        List<CreateDailyEntrySuccessResponseDto> createDailyEntrySuccessResponseDtoList,
        List<CreateDailyEntryFailureResponseDto> createDailyEntryFailureResponseDtoList) {

    public CreateDailyEntriesResponseDto(AddDailyEntriesResult result) {
        this(
                result.successList().stream().map(CreateDailyEntrySuccessResponseDto::new).toList(),
                result.failureList().stream().map(CreateDailyEntryFailureResponseDto::new).toList()
        );
    }

    record CreateDailyEntrySuccessResponseDto(Long id, GameSummaryDto game, PlayerSummaryDto player, LocalDate date, int points, String successCode) {

        CreateDailyEntrySuccessResponseDto(AddDailyEntrySuccess success) {
            this(
                    success.entry().dailyEntryId().id(),
                    new GameSummaryDto(success.entry().game().gameId().id(), success.entry().game().name()),
                    new PlayerSummaryDto(success.entry().player().playerId().id(), success.entry().player().name()),
                    success.entry().date(),
                    success.entry().points(),
                    success.successCode().name()
            );
        }
    }

    record CreateDailyEntryFailureResponseDto(GameSummaryDto game, PlayerSummaryDto player, LocalDate date, String failureCode) {

        CreateDailyEntryFailureResponseDto(AddDailyEntryFailure failure) {
            this(
                    new GameSummaryDto(failure.failureKey().game().gameId().id(), failure.failureKey().game().name()),
                    new PlayerSummaryDto(failure.failureKey().player().playerId().id(), failure.failureKey().player().name()),
                    failure.failureKey().date(),
                    failure.failureCode().name()
            );
        }
    }
}