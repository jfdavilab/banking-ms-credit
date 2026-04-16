package com.bank.credit.exception;

/**
 * Exception thrown when a business rule validation fails.
 */
public class BusinessValidationException extends RuntimeException {

    /**
     * Constructs a new BusinessValidationException with the specified message.
     *
     * @param message the detail message
     */
    public BusinessValidationException(String message) {
        super(message);
    }
}