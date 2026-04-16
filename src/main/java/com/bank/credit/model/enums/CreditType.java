package com.bank.credit.model.enums;

/**
 * Defines the types of credit products available in the banking system.
 */
public enum CreditType {
    /**
     * Personal credit loan.
     * Only one allowed per personal customer.
     */
    PERSONAL_CREDIT,

    /**
     * Business credit loan.
     * Multiple allowed per business customer.
     */
    BUSINESS_CREDIT,

    /**
     * Credit card product.
     * Multiple allowed for both personal and business customers.
     */
    CREDIT_CARD
}