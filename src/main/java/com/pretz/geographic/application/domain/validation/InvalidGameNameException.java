package com.pretz.geographic.application.domain.validation;

public class InvalidGameNameException extends RuntimeException {

    public InvalidGameNameException(String message) {
        super(message);
    }
}
