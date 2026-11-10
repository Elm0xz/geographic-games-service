package com.pretz.geographic.application.port.out.exception;

/**
 * Thrown by persistence adapters when a write conflicts with a concurrent modification of the same data.
 * Nothing from the conflicting write is persisted, and retrying the operation is safe.
 */
public class ConcurrentModificationConflictException extends RuntimeException {

    public ConcurrentModificationConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
