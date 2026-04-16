package com.bank.credit.exception;

/**
 * Exception thrown when a credit product is not found in the database.
 */
public class CreditNotFoundException extends RuntimeException {

    /**
     * Constructs a new CreditNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public CreditNotFoundException(String message) {
        super(message);
    }
}