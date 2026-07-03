package com.greenharvest.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception thrown when business logic validation fails
 * (e.g., attempting to log into a deactivated account or invalid credentials).
 * * Annotated with BAD_REQUEST (400) so that if it escapes the controller,
 * Spring handles it as a standard client-side validation failure.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}