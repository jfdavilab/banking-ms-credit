package com.bank.credit.repository;

import com.bank.credit.model.Credit;
import com.bank.credit.model.enums.CreditType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Credit entity.
 * Uses Spring Data derived queries — no @Query annotations.
 */
@Repository
public interface CreditRepository extends MongoRepository<Credit, String> {

    /**
     * Finds all credits belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @return list of credits for that customer
     */
    List<Credit> findByCustomerId(String customerId);

    /**
     * Finds all credits of a specific type for a customer.
     *
     * @param customerId the customer ID
     * @param creditType the type of credit
     * @return list of matching credits
     */
    List<Credit> findByCustomerIdAndCreditType(String customerId, CreditType creditType);

    /**
     * Counts credits of a given type for a customer.
     *
     * @param customerId the customer ID
     * @param creditType the credit type to count
     * @return the number of credits
     */
    long countByCustomerIdAndCreditType(String customerId, CreditType creditType);

    /**
     * Finds a credit by its unique credit number.
     *
     * @param creditNumber the credit number
     * @return an Optional with the credit if found
     */
    Optional<Credit> findByCreditNumber(String creditNumber);

    /**
     * Checks if a credit number already exists.
     *
     * @param creditNumber the credit number to check
     * @return true if it exists
     */
    boolean existsByCreditNumber(String creditNumber);

    /**
     * Finds all overdue credits for a specific customer.
     *
     * @param customerId the customer ID
     * @param isOverdue  true to find overdue credits
     * @return list of overdue credits
     */
    List<Credit> findByCustomerIdAndIsOverdue(String customerId, Boolean isOverdue);

    /**
     * Checks if a customer has any overdue credit.
     *
     * @param customerId the customer ID
     * @param isOverdue  true to check for overdue status
     * @return true if the customer has at least one overdue credit
     */
    boolean existsByCustomerIdAndIsOverdue(String customerId, Boolean isOverdue);
}