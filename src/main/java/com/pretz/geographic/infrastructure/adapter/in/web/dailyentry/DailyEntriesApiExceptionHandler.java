package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.domain.validation.InvalidGameNameException;
import com.pretz.geographic.application.domain.validation.InvalidPlayerNameException;
import com.pretz.geographic.application.port.out.exception.GameNotFoundException;
import com.pretz.geographic.application.port.out.exception.PlayerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.DAILY_ENTRY_ALREADY_EXISTS;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.GAME_NOT_FOUND;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.INVALID_GAME_NAME;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.INVALID_PLAYER_NAME;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.MALFORMED_REQUEST;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.PLAYER_NOT_FOUND;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyentry.DailyEntriesApiExceptionHandler.ApiErrorCode.VALIDATION_ERROR;

@RestControllerAdvice(basePackages = "com.pretz.geographic.infrastructure.adapter.in.web.dailyentry")
public class DailyEntriesApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DailyEntriesApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.warn("Request validation failed: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(VALIDATION_ERROR, "Request validation failed"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        log.warn("Request body could not be read: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(MALFORMED_REQUEST, "Request body is malformed or contains invalid field types"));
    }

    @ExceptionHandler(InvalidPlayerNameException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidPlayerName(InvalidPlayerNameException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(INVALID_PLAYER_NAME, exception.getMessage()));
    }

    @ExceptionHandler(InvalidGameNameException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidGameName(InvalidGameNameException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(INVALID_GAME_NAME, exception.getMessage()));
    }

    @ExceptionHandler(GameNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleGameNotFound(GameNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(GAME_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handlePlayerNotFound(PlayerNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(PLAYER_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Daily entry could not be saved due to data integrity violation");
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(DAILY_ENTRY_ALREADY_EXISTS, "Daily entry already exists"));
    }

    record ApiErrorResponse(ApiErrorCode code, String message) {
    }

    enum ApiErrorCode {
        VALIDATION_ERROR,
        MALFORMED_REQUEST,
        INVALID_PLAYER_NAME,
        INVALID_GAME_NAME,
        GAME_NOT_FOUND,
        PLAYER_NOT_FOUND,
        DAILY_ENTRY_ALREADY_EXISTS
    }
}
