package com.bank.credit.service.impl;

import com.bank.credit.client.CustomerClient;
import com.bank.credit.dto.CreditRequestDto;
import com.bank.credit.dto.CreditResponseDto;
import com.bank.credit.exception.BusinessValidationException;
import com.bank.credit.exception.CreditNotFoundException;
import com.bank.credit.mapper.CreditMapper;
import com.bank.credit.model.Credit;
import com.bank.credit.model.enums.CreditType;
import com.bank.credit.repository.CreditRepository;
import com.bank.credit.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CreditService with full business rule validation.
 *
 * <p>Business rules enforced:</p>
 * <ul>
 *   <li>PERSONAL customer: max 1 PERSONAL_CREDIT, unlimited CREDIT_CARD</li>
 *   <li>BUSINESS customer: unlimited BUSINESS_CREDIT, unlimited CREDIT_CARD</li>
 *   <li>PERSONAL customer cannot request BUSINESS_CREDIT</li>
 *   <li>BUSINESS customer cannot request PERSONAL_CREDIT</li>
 *   <li>Credit card charges cannot exceed available balance</li>
 *   <li>Payments cannot exceed outstanding balance</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CustomerClient customerClient;

    /** Customer type constant for personal customers. */
    private static final String CUSTOMER_TYPE_PERSONAL = "PERSONAL";

    /** Customer type constant for business customers. */
    private static final String CUSTOMER_TYPE_BUSINESS = "BUSINESS";

    /**
     * {@inheritDoc}
     * Validates customer existence, type compatibility and credit limits
     * before persisting the new credit product.
     */
    @Override
    public CreditResponseDto createCredit(CreditRequestDto requestDto) {
        log.info("Creating credit type {} for customerId: {}",
                requestDto.getCreditType(), requestDto.getCustomerId());

        // Validate customer exists and retrieve their type
        Map<String, Object> customer = customerClient.findCustomerById(
                requestDto.getCustomerId());
        String customerType = (String) customer.get("customerType");

        // Validate business rules based on customer type and credit type
        validateCreditCreationRules(requestDto, customerType);

        // Generate unique credit number
        String creditNumber = generateCreditNumber();

        Credit credit = CreditMapper.toEntity(requestDto, creditNumber);
        Credit savedCredit = creditRepository.save(credit);

        log.info("Credit created successfully with number: {}", savedCredit.getCreditNumber());
        return CreditMapper.toDto(savedCredit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CreditResponseDto> findAllCredits() {
        log.info("Retrieving all credits");
        return creditRepository.findAll()
                .stream()
                .map(CreditMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreditResponseDto findCreditById(String id) {
        log.info("Searching credit by id: {}", id);
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit not found with id: {}", id);
                    return new CreditNotFoundException("Credit not found with id: " + id);
                });
        return CreditMapper.toDto(credit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreditResponseDto findCreditByCreditNumber(String creditNumber) {
        log.info("Searching credit by number: {}", creditNumber);
        Credit credit = creditRepository.findByCreditNumber(creditNumber)
                .orElseThrow(() -> {
                    log.error("Credit not found with number: {}", creditNumber);
                    return new CreditNotFoundException(
                            "Credit not found with number: " + creditNumber);
                });
        return CreditMapper.toDto(credit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CreditResponseDto> findCreditsByCustomerId(String customerId) {
        log.info("Retrieving credits for customerId: {}", customerId);
        return creditRepository.findByCustomerId(customerId)
                .stream()
                .map(CreditMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreditResponseDto updateCredit(String id, CreditRequestDto requestDto) {
        log.info("Updating credit with id: {}", id);

        Credit existing = creditRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit not found with id: {}", id);
                    return new CreditNotFoundException("Credit not found with id: " + id);
                });

        Credit updated = CreditMapper.updateEntity(existing, requestDto);
        Credit savedCredit = creditRepository.save(updated);

        log.info("Credit updated successfully with id: {}", savedCredit.getId());
        return CreditMapper.toDto(savedCredit);
    }

    /**
     * {@inheritDoc}
     * Performs a logical delete (sets status to false).
     */
    @Override
    public void deleteCredit(String id) {
        log.info("Deactivating credit with id: {}", id);

        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit not found with id: {}", id);
                    return new CreditNotFoundException("Credit not found with id: " + id);
                });

        credit.setStatus(false);
        creditRepository.save(credit);
        log.info("Credit deactivated successfully with id: {}", id);
    }

    /**
     * {@inheritDoc}
     * Only applicable to CREDIT_CARD type credits.
     */
    @Override
    public BigDecimal getAvailableBalance(String id) {
        log.info("Getting available balance for credit id: {}", id);
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new CreditNotFoundException(
                        "Credit not found with id: " + id));

        if (credit.getCreditType() != CreditType.CREDIT_CARD) {
            throw new BusinessValidationException(
                    "Available balance is only applicable to credit cards");
        }
        return credit.getAvailableBalance();
    }

    /**
     * {@inheritDoc}
     * Validates that the charge does not exceed the available balance.
     * Only applicable to CREDIT_CARD type credits.
     */
    @Override
    public CreditResponseDto applyCharge(String id, BigDecimal amount) {
        log.info("Applying charge of {} to credit card id: {}", amount, id);

        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new CreditNotFoundException(
                        "Credit not found with id: " + id));

        // Validate credit type
        if (credit.getCreditType() != CreditType.CREDIT_CARD) {
            throw new BusinessValidationException(
                    "Charges can only be applied to credit cards");
        }

        // Validate active status
        if (!credit.getStatus()) {
            throw new BusinessValidationException(
                    "Cannot apply charge to an inactive credit card");
        }

        // Validate sufficient available balance
        if (credit.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessValidationException(
                    "Insufficient available balance. Available: "
                            + credit.getAvailableBalance() + ", Requested: " + amount);
        }

        // Apply the charge
        credit.setOutstandingBalance(credit.getOutstandingBalance().add(amount));
        credit.setAvailableBalance(credit.getAvailableBalance().subtract(amount));

        Credit savedCredit = creditRepository.save(credit);
        log.info("Charge applied successfully to credit card id: {}", id);
        return CreditMapper.toDto(savedCredit);
    }

    /**
     * {@inheritDoc}
     * Validates that the payment does not exceed the outstanding balance.
     * Updates overdue status if balance reaches zero.
     */
    @Override
    public CreditResponseDto applyPayment(String id, BigDecimal amount) {
        log.info("Applying payment of {} to credit id: {}", amount, id);

        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new CreditNotFoundException(
                        "Credit not found with id: " + id));

        // Validate active status
        if (!credit.getStatus()) {
            throw new BusinessValidationException(
                    "Cannot apply payment to an inactive credit");
        }

        // Validate payment amount does not exceed outstanding balance
        if (credit.getOutstandingBalance().compareTo(amount) < 0) {
            throw new BusinessValidationException(
                    "Payment amount exceeds outstanding balance. Outstanding: "
                            + credit.getOutstandingBalance() + ", Payment: " + amount);
        }

        // Apply the payment
        credit.setOutstandingBalance(credit.getOutstandingBalance().subtract(amount));

        // Update available balance for credit cards
        if (credit.getCreditType() == CreditType.CREDIT_CARD) {
            credit.setAvailableBalance(credit.getAvailableBalance().add(amount));
        }

        // Clear overdue flag if balance is fully paid
        if (credit.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
            credit.setIsOverdue(false);
            log.info("Credit id {} fully paid — overdue flag cleared", id);
        }

        Credit savedCredit = creditRepository.save(credit);
        log.info("Payment applied successfully to credit id: {}", id);
        return CreditMapper.toDto(savedCredit);
    }

    // ─────────────────────────────────────────────
    //  Private validation methods
    // ─────────────────────────────────────────────

    /**
     * Validates credit creation rules based on customer type and credit type.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>PERSONAL customer: only PERSONAL_CREDIT (max 1) or CREDIT_CARD</li>
     *   <li>BUSINESS customer: only BUSINESS_CREDIT or CREDIT_CARD</li>
     * </ul>
     *
     * @param requestDto   the incoming credit request
     * @param customerType the customer type from ms-customer
     */
    private void validateCreditCreationRules(CreditRequestDto requestDto,
                                             String customerType) {
        CreditType creditType = requestDto.getCreditType();
        String customerId = requestDto.getCustomerId();

        if (CUSTOMER_TYPE_PERSONAL.equals(customerType)) {
            validatePersonalCreditRules(customerId, creditType);
        } else if (CUSTOMER_TYPE_BUSINESS.equals(customerType)) {
            validateBusinessCreditRules(creditType);
        } else {
            throw new BusinessValidationException(
                    "Unknown customer type: " + customerType);
        }
    }

    /**
     * Validates credit rules for personal customers.
     * Personal customers can have max 1 PERSONAL_CREDIT and cannot have BUSINESS_CREDIT.
     *
     * @param customerId the customer ID
     * @param creditType the type of credit being requested
     */
    private void validatePersonalCreditRules(String customerId, CreditType creditType) {
        if (creditType == CreditType.BUSINESS_CREDIT) {
            throw new BusinessValidationException(
                    "Personal customers cannot request business credits");
        }

        if (creditType == CreditType.PERSONAL_CREDIT) {
            long count = creditRepository.countByCustomerIdAndCreditType(
                    customerId, CreditType.PERSONAL_CREDIT);
            if (count >= 1) {
                throw new BusinessValidationException(
                        "Personal customers can only have one personal credit");
            }
        }
    }

    /**
     * Validates credit rules for business customers.
     * Business customers cannot have PERSONAL_CREDIT.
     *
     * @param creditType the type of credit being requested
     */
    private void validateBusinessCreditRules(CreditType creditType) {
        if (creditType == CreditType.PERSONAL_CREDIT) {
            throw new BusinessValidationException(
                    "Business customers cannot request personal credits");
        }
    }

    /**
     * Generates a unique credit number using UUID.
     * Ensures no collision with existing credit numbers.
     *
     * @return a unique 12-character credit number string
     */
    private String generateCreditNumber() {
        String creditNumber;
        do {
            creditNumber = "CRD-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (creditRepository.existsByCreditNumber(creditNumber));
        return creditNumber;
    }
}