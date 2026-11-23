package com.pretz.geographic.infrastructure.adapter.out.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

/**
 * Recognizes unique constraint violations in exceptions thrown by Spring Data repositories.
 */
public final class UniqueViolations {

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private UniqueViolations() {
    }

    public static boolean isUniqueViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException && sqlException.getSQLState() != null) {
                return UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState());
            }
        }
        return false;
    }

    public static boolean isUniqueViolationOf(DataIntegrityViolationException exception, String constraintName) {
        if (!isUniqueViolation(exception)) {
            return false;
        }
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return constraintName.equalsIgnoreCase(violation.getConstraintName());
            }
        }
        return false;
    }
}
