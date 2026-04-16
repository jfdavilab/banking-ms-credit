package com.bank.credit.mapper;

import com.bank.credit.dto.CreditRequestDto;
import com.bank.credit.dto.CreditResponseDto;
import com.bank.credit.model.Credit;
import com.bank.credit.model.enums.CreditType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Utility class for mapping between Credit entity and DTOs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreditMapper {

    /**
     * Converts a CreditRequestDto to a Credit entity.
     * Sets initial balances based on credit type.
     *
     * @param dto          the incoming request DTO
     * @param creditNumber the generated unique credit number
     * @return a new Credit entity
     */
    public static Credit toEntity(CreditRequestDto dto, String creditNumber) {
        BigDecimal availableBalance = dto.getCreditType() == CreditType.CREDIT_CARD
                ? dto.getCreditLimit()
                : BigDecimal.ZERO;

        return Credit.builder()
                .creditNumber(creditNumber)
                .creditType(dto.getCreditType())
                .customerId(dto.getCustomerId())
                .creditLimit(dto.getCreditLimit())
                .outstandingBalance(BigDecimal.ZERO)
                .availableBalance(availableBalance)
                .isOverdue(false)
                .dueDate(dto.getDueDate())
                .status(true)
                .build();
    }

    /**
     * Converts a Credit entity to a CreditResponseDto.
     *
     * @param credit the credit entity from the database
     * @return the response DTO
     */
    public static CreditResponseDto toDto(Credit credit) {
        return CreditResponseDto.builder()
                .id(credit.getId())
                .creditNumber(credit.getCreditNumber())
                .creditType(credit.getCreditType())
                .customerId(credit.getCustomerId())
                .creditLimit(credit.getCreditLimit())
                .outstandingBalance(credit.getOutstandingBalance())
                .availableBalance(credit.getAvailableBalance())
                .isOverdue(credit.getIsOverdue())
                .dueDate(credit.getDueDate())
                .status(credit.getStatus())
                .createdAt(credit.getCreatedAt())
                .updatedAt(credit.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing Credit entity with data from a request DTO.
     * Only mutable fields are updated (limit and due date).
     *
     * @param existing the existing credit entity
     * @param dto      the incoming request DTO
     * @return the updated Credit entity
     */
    public static Credit updateEntity(Credit existing, CreditRequestDto dto) {
        existing.setCreditLimit(dto.getCreditLimit());
        existing.setDueDate(dto.getDueDate());

        // Recalculate available balance for credit cards
        if (existing.getCreditType() == CreditType.CREDIT_CARD) {
            BigDecimal newAvailable = dto.getCreditLimit()
                    .subtract(existing.getOutstandingBalance());
            existing.setAvailableBalance(newAvailable.compareTo(BigDecimal.ZERO) < 0
                    ? BigDecimal.ZERO
                    : newAvailable);
        }
        return existing;
    }
}