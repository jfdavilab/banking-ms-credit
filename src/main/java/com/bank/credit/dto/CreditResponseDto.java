package com.bank.credit.dto;

import com.bank.credit.model.enums.CreditType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for outgoing credit data in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponseDto {

    /** Unique credit identifier. */
    private String id;

    /** Unique credit number. */
    private String creditNumber;

    /** Type of credit product. */
    private CreditType creditType;

    /** ID of the customer owner. */
    private String customerId;

    /** Total approved credit limit. */
    private BigDecimal creditLimit;

    /** Outstanding balance owed. */
    private BigDecimal outstandingBalance;

    /** Available balance (credit cards only). */
    private BigDecimal availableBalance;

    /** Indicates if the credit is overdue. */
    private Boolean isOverdue;

    /** Due date for the credit product. */
    private LocalDateTime dueDate;

    /** Indicates if the credit is active. */
    private Boolean status;

    /** Timestamp when the credit was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the credit was last updated. */
    private LocalDateTime updatedAt;
}