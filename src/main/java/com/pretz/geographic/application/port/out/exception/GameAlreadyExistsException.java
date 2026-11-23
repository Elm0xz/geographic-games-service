package com.pretz.geographic.application.port.out.exception;

public class GameAlreadyExistsException extends RuntimeException {

    private static final String GAME_ALREADY_EXISTS_MESSAGE = "Game with name %s already exists";

    public GameAlreadyExistsException(String name, Throwable cause) {
        super(String.format(GAME_ALREADY_EXISTS_MESSAGE, name), cause);
    }
}
