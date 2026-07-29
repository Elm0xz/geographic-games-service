package com.pretz.geographic.application.port.out.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(Long id) {
        super("Player not found for id: " + id);
    }

    public PlayerNotFoundException(String name) {
        super("Player not found for name: " + name);
    }
}
