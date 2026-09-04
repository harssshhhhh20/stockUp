package com.stockup.backend.domain.user.exception;

import com.stockup.backend.common.exceptions.model.ConflictException;

/**
 * Raised when an account tries to switch sides of the marketplace. The choice
 * is made once at sign-up and is deliberately permanent.
 */
public class RoleAlreadyChosenException extends ConflictException {

    public RoleAlreadyChosenException(String message) {
        super(message);
    }
}
