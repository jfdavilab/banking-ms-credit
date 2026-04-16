package com.bank.credit.service;

import com.bank.credit.dto.CreditRequestDto;
import com.bank.credit.dto.CreditResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface defining all business operations for credit products.
 */
public interface CreditService {

    /**
     * Creates a new credit product after validating all business rules.
     *
     * @param requestDto the credit data to create
     * @return the created credit as a response DTO
     */
    CreditResponseDto createCredit(CreditRequestDto requestDto);

    /**
     * Retrieves all credits in the system.
     *
     * @return list of all credits
     */
    List<CreditResponseDto> findAllCredits();

    /**
     * Finds a credit by its unique ID.
     *
     * @param id the credit MongoDB ID
     * @return the credit as a response DTO
     */
    CreditResponseDto findCreditById(String id);

    /**
     * Finds a credit by its credit number.
     *
     * @param creditNumber the unique credit number
     * @return the credit as a response DTO
     */
    CreditResponseDto findCreditByCreditNumber(String creditNumber);

    /**
     * Finds all credits belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @return list of credits for that customer
     */
    List<CreditResponseDto> findCreditsByCustomerId(String customerId);

    /**
     * Updates an existing credit's configuration.
     *
     * @param id         the credit ID to update
     * @param requestDto the new credit data
     * @return the updated credit as a response DTO
     */
    CreditResponseDto updateCredit(String id, CreditRequestDto requestDto);

    /**
     * Performs a logical delete by setting credit status to inactive.
     *
     * @param id the credit ID to deactivate
     */
    void deleteCredit(String id);

    /**
     * Returns the available balance for a credit card.
     *
     * @param id the credit ID
     * @return the available balance
     */
    BigDecimal getAvailableBalance(String id);

    /**
     * Applies a charge to a credit card, reducing available balance.
     *
     * @param id     the credit card ID
     * @param amount the amount to charge
     * @return the updated credit as a response DTO
     */
    CreditResponseDto applyCharge(String id, BigDecimal amount);

    /**
     * Applies a payment to a credit, reducing outstanding balance.
     *
     * @param id     the credit ID
     * @param amount the payment amount
     * @return the updated credit as a response DTO
     */
    CreditResponseDto applyPayment(String id, BigDecimal amount);
}