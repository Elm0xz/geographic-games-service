package com.pretz.geographic.application.port.out.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("Game not found for id: " + id);
    }
}
