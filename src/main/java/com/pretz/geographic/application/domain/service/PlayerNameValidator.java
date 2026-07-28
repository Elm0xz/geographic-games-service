package com.pretz.geographic.application.domain.service;

public class PlayerNameValidator {

    public void validate(String inputName, String persistedName) {
        if (!persistedName.equals(inputName)) {
            throw new InvalidPlayerNameException(String.format("Input game name: %s doesn't match persisted game name: %s",
                    inputName, persistedName));
        }
    }
}
