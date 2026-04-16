package com.bank.credit.dto;

import com.bank.credit.model.enums.CreditType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for incoming credit creation and update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequestDto {

    /** Type of credit product to create. */
    @NotNull(message = "Credit type is required")
    private CreditType creditType;

    /** ID of the customer requesting the credit. */
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /**
     * Approved credit limit or loan amount.
     * Must be greater than zero.
     */
    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "1.0", message = "Credit limit must be greater than zero")
    private BigDecimal creditLimit;

    /**
     * Due date for the credit product.
     * Optional at creation, can be set later.
     */
    private LocalDateTime dueDate;
}