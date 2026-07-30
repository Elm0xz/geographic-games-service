package com.pretz.geographic.infrastructure.adapter.in.web.dailyentry;

import com.pretz.geographic.application.domain.validation.InvalidGameNameException;
import com.pretz.geographic.application.domain.validation.InvalidPlayerNameException;
import com.pretz.geographic.application.port.out.exception.GameNotFoundException;
import com.pretz.geographic.application.port.out.exception.PlayerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DailyEntriesApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DailyEntriesApiExceptionHandler.class);

    @ExceptionHandler(InvalidPlayerNameException.class)
    ResponseEntity<ErrorResponse> handleInvalidPlayerName(InvalidPlayerNameException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(InvalidGameNameException.class)
    ResponseEntity<ErrorResponse> handleInvalidGameName(InvalidGameNameException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(GameNotFoundException.class)
    ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    ResponseEntity<ErrorResponse> handlePlayerNotFound(PlayerNotFoundException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.badRequest().build();
    }
}
