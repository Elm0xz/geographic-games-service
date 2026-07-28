package com.pretz.geographic.application.domain.service;

public class InvalidPlayerNameException extends RuntimeException {
    public InvalidPlayerNameException(String message) {
        super(message);
    }
}
