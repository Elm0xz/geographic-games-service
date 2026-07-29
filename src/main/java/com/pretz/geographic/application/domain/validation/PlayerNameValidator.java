package com.pretz.geographic.application.domain.validation;

public class PlayerNameValidator {

    public void validate(String inputName, String persistedName) {
        if (!persistedName.equals(inputName)) {
            throw new InvalidPlayerNameException(String.format("Input player name: %s doesn't match persisted player name: %s",
                    inputName, persistedName));
        }
    }
}
