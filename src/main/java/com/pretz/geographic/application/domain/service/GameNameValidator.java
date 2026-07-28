package com.pretz.geographic.application.domain.service;

public class GameNameValidator {

    public void validate(String inputName, String persistedName) {
        if (!persistedName.equals(inputName)) {
            throw new InvalidGameNameException(String.format("Input game name: %s doesn't match persisted game name: %s",
                    inputName, persistedName));
        }
    }
}
