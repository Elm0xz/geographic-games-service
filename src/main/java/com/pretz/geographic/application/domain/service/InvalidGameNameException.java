package com.pretz.geographic.application.domain.service;

public class InvalidGameNameException extends RuntimeException {

    public InvalidGameNameException(String message) {
        super(message);
    }
}
