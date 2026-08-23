package com.pretz.geographic.infrastructure.adapter.in.web.dailyranking;

import com.pretz.geographic.application.domain.validation.InvalidDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.DailyRankingApiExceptionHandler.ApiErrorCode.INVALID_DATE;
import static com.pretz.geographic.infrastructure.adapter.in.web.dailyranking.DailyRankingApiExceptionHandler.ApiErrorCode.MALFORMED_REQUEST;

@RestControllerAdvice
public class DailyRankingApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DailyRankingApiExceptionHandler.class);

    @ExceptionHandler(InvalidDateException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(InvalidDateException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(INVALID_DATE, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Request parameter type mismatch: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(MALFORMED_REQUEST, "Request parameter is malformed or has invalid type"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(MALFORMED_REQUEST, "Required request parameter is missing"));
    }

    record ApiErrorResponse(ApiErrorCode code, String message) {
    }

    enum ApiErrorCode {
        INVALID_DATE,
        MALFORMED_REQUEST
    }
}
