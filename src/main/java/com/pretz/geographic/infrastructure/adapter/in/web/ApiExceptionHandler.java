package com.pretz.geographic.infrastructure.adapter.in.web;

import com.pretz.geographic.application.domain.validation.InvalidDateException;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.warn("Request validation failed: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.VALIDATION_ERROR, "Request validation failed"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        log.warn("Request body could not be read: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.MALFORMED_REQUEST, "Request body is malformed or contains invalid field types"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Request parameter type mismatch: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.MALFORMED_REQUEST, "Request parameter is malformed or has invalid type"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.MALFORMED_REQUEST, "Required request parameter is missing"));
    }

    @ExceptionHandler(InvalidDateException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidDate(InvalidDateException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.INVALID_DATE, exception.getMessage()));
    }

    @ExceptionHandler(InvalidPlayerNameException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidPlayerName(InvalidPlayerNameException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.INVALID_PLAYER_NAME, exception.getMessage()));
    }

    @ExceptionHandler(InvalidGameNameException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidGameName(InvalidGameNameException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.INVALID_GAME_NAME, exception.getMessage()));
    }

    @ExceptionHandler(GameNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleGameNotFound(GameNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.GAME_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handlePlayerNotFound(PlayerNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.PLAYER_NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Daily entry could not be saved due to data integrity violation");
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.DAILY_ENTRY_ALREADY_EXISTS, "Daily entry already exists"));
    }

    record ApiErrorResponse(ApiErrorCode code, String message) {
    }

    enum ApiErrorCode {
        VALIDATION_ERROR,
        MALFORMED_REQUEST,
        INVALID_DATE,
        INVALID_PLAYER_NAME,
        INVALID_GAME_NAME,
        GAME_NOT_FOUND,
        PLAYER_NOT_FOUND,
        DAILY_ENTRY_ALREADY_EXISTS
    }
}
