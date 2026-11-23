package com.pretz.geographic.infrastructure.adapter.in.web;

/**
 * Syntactic rules for names of games and players: letters (any alphabet), digits, spaces and {@code _ . ' -},
 * starting and ending with a letter or digit.
 */
public final class NameConstraints {

    public static final String PATTERN = "^[\\p{L}\\p{N}](?:[\\p{L}\\p{N} _.'-]*[\\p{L}\\p{N}])?$";
    public static final int MAX_LENGTH = 50;

    private NameConstraints() {
    }
}
