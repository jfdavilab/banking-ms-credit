package com.bank.credit.controller;

import com.bank.credit.dto.CreditRequestDto;
import com.bank.credit.dto.CreditResponseDto;
import com.bank.credit.service.CreditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for credit product management operations.
 * Exposes endpoints for CRUD and financial operations on credits.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    /**
     * Creates a new credit product.
     * POST /api/v1/credits
     *
     * @param requestDto the credit data
     * @return HTTP 201 with the created credit
     */
    @PostMapping
    public ResponseEntity<CreditResponseDto> createCredit(
            @Valid @RequestBody CreditRequestDto requestDto) {
        log.info("POST /api/v1/credits - Creating credit type: {}", requestDto.getCreditType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(creditService.createCredit(requestDto));
    }

    /**
     * Retrieves all credits.
     * GET /api/v1/credits
     *
     * @return HTTP 200 with list of all credits
     */
    @GetMapping
    public ResponseEntity<List<CreditResponseDto>> getAllCredits() {
        log.info("GET /api/v1/credits - Retrieving all credits");
        return ResponseEntity.ok(creditService.findAllCredits());
    }

    /**
     * Retrieves a credit by its ID.
     * GET /api/v1/credits/{id}
     *
     * @param id the credit ID
     * @return HTTP 200 with the credit data
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditResponseDto> getCreditById(@PathVariable String id) {
        log.info("GET /api/v1/credits/{} - Retrieving credit", id);
        return ResponseEntity.ok(creditService.findCreditById(id));
    }

    /**
     * Retrieves a credit by its credit number.
     * GET /api/v1/credits/number/{creditNumber}
     *
     * @param creditNumber the unique credit number
     * @return HTTP 200 with the credit data
     */
    @GetMapping("/number/{creditNumber}")
    public ResponseEntity<CreditResponseDto> getCreditByNumber(
            @PathVariable String creditNumber) {
        log.info("GET /api/v1/credits/number/{} - Retrieving credit", creditNumber);
        return ResponseEntity.ok(creditService.findCreditByCreditNumber(creditNumber));
    }

    /**
     * Retrieves all credits for a specific customer.
     * GET /api/v1/credits/customer/{customerId}
     *
     * @param customerId the customer ID
     * @return HTTP 200 with list of customer credits
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditResponseDto>> getCreditsByCustomerId(
            @PathVariable String customerId) {
        log.info("GET /api/v1/credits/customer/{} - Retrieving credits", customerId);
        return ResponseEntity.ok(creditService.findCreditsByCustomerId(customerId));
    }

    /**
     * Retrieves the available balance for a credit card.
     * GET /api/v1/credits/{id}/available-balance
     *
     * @param id the credit card ID
     * @return HTTP 200 with the available balance
     */
    @GetMapping("/{id}/available-balance")
    public ResponseEntity<BigDecimal> getAvailableBalance(@PathVariable String id) {
        log.info("GET /api/v1/credits/{}/available-balance", id);
        return ResponseEntity.ok(creditService.getAvailableBalance(id));
    }

    /**
     * Updates an existing credit product.
     * PUT /api/v1/credits/{id}
     *
     * @param id         the credit ID to update
     * @param requestDto the updated credit data
     * @return HTTP 200 with the updated credit
     */
    @PutMapping("/{id}")
    public ResponseEntity<CreditResponseDto> updateCredit(
            @PathVariable String id,
            @Valid @RequestBody CreditRequestDto requestDto) {
        log.info("PUT /api/v1/credits/{} - Updating credit", id);
        return ResponseEntity.ok(creditService.updateCredit(id, requestDto));
    }

    /**
     * Deactivates a credit product (logical delete).
     * DELETE /api/v1/credits/{id}
     *
     * @param id the credit ID to deactivate
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredit(@PathVariable String id) {
        log.info("DELETE /api/v1/credits/{} - Deactivating credit", id);
        creditService.deleteCredit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Applies a charge to a credit card.
     * PATCH /api/v1/credits/{id}/charge
     *
     * @param id     the credit card ID
     * @param amount the amount to charge
     * @return HTTP 200 with the updated credit card data
     */
    @PatchMapping("/{id}/charge")
    public ResponseEntity<CreditResponseDto> applyCharge(
            @PathVariable String id,
            @RequestParam @DecimalMin(value = "0.01",
                    message = "Charge amount must be greater than zero") BigDecimal amount) {
        log.info("PATCH /api/v1/credits/{}/charge - Applying charge: {}", id, amount);
        return ResponseEntity.ok(creditService.applyCharge(id, amount));
    }

    /**
     * Applies a payment to a credit product.
     * PATCH /api/v1/credits/{id}/payment
     *
     * @param id     the credit ID
     * @param amount the payment amount
     * @return HTTP 200 with the updated credit data
     */
    @PatchMapping("/{id}/payment")
    public ResponseEntity<CreditResponseDto> applyPayment(
            @PathVariable String id,
            @RequestParam @DecimalMin(value = "0.01",
                    message = "Payment amount must be greater than zero") BigDecimal amount) {
        log.info("PATCH /api/v1/credits/{}/payment - Applying payment: {}", id, amount);
        return ResponseEntity.ok(creditService.applyPayment(id, amount));
    }
}