package com.pretz.geographic.application.port.out.exception;

public class PlayerAlreadyExistsException extends RuntimeException {

    private static final String PLAYER_ALREADY_EXISTS_MESSAGE = "Player with name %s already exists";

    public PlayerAlreadyExistsException(String name, Throwable cause) {
        super(String.format(PLAYER_ALREADY_EXISTS_MESSAGE, name), cause);
    }
}
