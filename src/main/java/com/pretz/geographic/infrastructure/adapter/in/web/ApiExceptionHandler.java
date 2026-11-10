package com.pretz.geographic.infrastructure.adapter.in.web;

import com.pretz.geographic.application.domain.validation.InvalidDateException;
import com.pretz.geographic.application.port.out.exception.ConcurrentModificationConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.CONFLICT;

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

    @ExceptionHandler(ConcurrentModificationConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConcurrentModificationConflict(ConcurrentModificationConflictException exception) {
        log.warn("Concurrent modification conflict: {}", exception.getMessage());
        return ResponseEntity.status(CONFLICT)
                .body(new ApiErrorResponse(ApiErrorCode.CONCURRENT_MODIFICATION,
                        "Request conflicted with a concurrent modification and was not saved; retry it"));
    }

    record ApiErrorResponse(ApiErrorCode code, String message) {
    }

    enum ApiErrorCode {
        VALIDATION_ERROR,
        MALFORMED_REQUEST,
        INVALID_DATE,
        CONCURRENT_MODIFICATION
    }
}
