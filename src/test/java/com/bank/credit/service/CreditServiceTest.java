package com.bank.credit.service;

import com.bank.credit.client.CustomerClient;
import com.bank.credit.dto.CreditRequestDto;
import com.bank.credit.dto.CreditResponseDto;
import com.bank.credit.exception.BusinessValidationException;
import com.bank.credit.exception.CreditNotFoundException;
import com.bank.credit.model.Credit;
import com.bank.credit.model.enums.CreditType;
import com.bank.credit.repository.CreditRepository;
import com.bank.credit.service.impl.CreditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreditServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private CreditServiceImpl creditService;

    private CreditRequestDto personalCreditRequest;
    private CreditRequestDto businessCreditRequest;
    private CreditRequestDto creditCardRequest;
    private Credit savedPersonalCredit;
    private Credit savedCreditCard;

    @BeforeEach
    void setUp() {
        personalCreditRequest = CreditRequestDto.builder()
                .creditType(CreditType.PERSONAL_CREDIT)
                .customerId("customer123")
                .creditLimit(new BigDecimal("5000.00"))
                .build();

        businessCreditRequest = CreditRequestDto.builder()
                .creditType(CreditType.BUSINESS_CREDIT)
                .customerId("business456")
                .creditLimit(new BigDecimal("50000.00"))
                .build();

        creditCardRequest = CreditRequestDto.builder()
                .creditType(CreditType.CREDIT_CARD)
                .customerId("customer123")
                .creditLimit(new BigDecimal("3000.00"))
                .build();

        savedPersonalCredit = Credit.builder()
                .id("crd001")
                .creditNumber("CRD-ABC12345")
                .creditType(CreditType.PERSONAL_CREDIT)
                .customerId("customer123")
                .creditLimit(new BigDecimal("5000.00"))
                .outstandingBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .isOverdue(false)
                .status(true)
                .build();

        savedCreditCard = Credit.builder()
                .id("crd002")
                .creditNumber("CRD-XYZ67890")
                .creditType(CreditType.CREDIT_CARD)
                .customerId("customer123")
                .creditLimit(new BigDecimal("3000.00"))
                .outstandingBalance(BigDecimal.ZERO)
                .availableBalance(new BigDecimal("3000.00"))
                .isOverdue(false)
                .status(true)
                .build();
    }

    // ─── CREATE TESTS ───────────────────────────────

    @Test
    @DisplayName("Should create personal credit successfully")
    void createCredit_PersonalCredit_Success() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));
        when(creditRepository.countByCustomerIdAndCreditType(
                anyString(), eq(CreditType.PERSONAL_CREDIT))).thenReturn(0L);
        when(creditRepository.existsByCreditNumber(anyString())).thenReturn(false);
        when(creditRepository.save(any(Credit.class))).thenReturn(savedPersonalCredit);

        CreditResponseDto result = creditService.createCredit(personalCreditRequest);

        assertNotNull(result);
        assertEquals(CreditType.PERSONAL_CREDIT, result.getCreditType());
        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    @DisplayName("Should throw exception when personal customer already has personal credit")
    void createCredit_PersonalDuplicate_ThrowsException() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));
        when(creditRepository.countByCustomerIdAndCreditType(
                anyString(), eq(CreditType.PERSONAL_CREDIT))).thenReturn(1L);

        assertThrows(BusinessValidationException.class,
                () -> creditService.createCredit(personalCreditRequest));
        verify(creditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when personal customer requests business credit")
    void createCredit_PersonalRequestsBusiness_ThrowsException() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));

        CreditRequestDto request = CreditRequestDto.builder()
                .creditType(CreditType.BUSINESS_CREDIT)
                .customerId("customer123")
                .creditLimit(new BigDecimal("10000.00"))
                .build();

        assertThrows(BusinessValidationException.class,
                () -> creditService.createCredit(request));
    }

    @Test
    @DisplayName("Should throw exception when business customer requests personal credit")
    void createCredit_BusinessRequestsPersonal_ThrowsException() {
        when(customerClient.findCustomerById("business456"))
                .thenReturn(Map.of("customerType", "BUSINESS"));

        CreditRequestDto request = CreditRequestDto.builder()
                .creditType(CreditType.PERSONAL_CREDIT)
                .customerId("business456")
                .creditLimit(new BigDecimal("5000.00"))
                .build();

        assertThrows(BusinessValidationException.class,
                () -> creditService.createCredit(request));
    }

    @Test
    @DisplayName("Should create credit card with full available balance")
    void createCredit_CreditCard_AvailableBalanceEqualsLimit() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));
        when(creditRepository.existsByCreditNumber(anyString())).thenReturn(false);
        when(creditRepository.save(any(Credit.class))).thenReturn(savedCreditCard);

        CreditResponseDto result = creditService.createCredit(creditCardRequest);

        assertNotNull(result);
        assertEquals(new BigDecimal("3000.00"), result.getAvailableBalance());
    }

    // ─── CHARGE TESTS ───────────────────────────────

    @Test
    @DisplayName("Should apply charge to credit card successfully")
    void applyCharge_Success() {
        when(creditRepository.findById("crd002"))
                .thenReturn(Optional.of(savedCreditCard));
        when(creditRepository.save(any(Credit.class))).thenReturn(savedCreditCard);

        CreditResponseDto result = creditService.applyCharge("crd002",
                new BigDecimal("500.00"));

        assertNotNull(result);
        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    @DisplayName("Should throw exception when charge exceeds available balance")
    void applyCharge_InsufficientBalance_ThrowsException() {
        when(creditRepository.findById("crd002"))
                .thenReturn(Optional.of(savedCreditCard));

        assertThrows(BusinessValidationException.class,
                () -> creditService.applyCharge("crd002",
                        new BigDecimal("9999.00")));
    }

    @Test
    @DisplayName("Should throw exception when applying charge to non credit card")
    void applyCharge_NotCreditCard_ThrowsException() {
        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));

        assertThrows(BusinessValidationException.class,
                () -> creditService.applyCharge("crd001",
                        new BigDecimal("100.00")));
    }

    // ─── PAYMENT TESTS ───────────────────────────────

    @Test
    @DisplayName("Should apply payment to credit successfully")
    void applyPayment_Success() {
        savedPersonalCredit.setOutstandingBalance(new BigDecimal("1000.00"));

        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));
        when(creditRepository.save(any(Credit.class))).thenReturn(savedPersonalCredit);

        CreditResponseDto result = creditService.applyPayment("crd001",
                new BigDecimal("500.00"));

        assertNotNull(result);
        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    @DisplayName("Should clear overdue flag when credit fully paid")
    void applyPayment_FullPayment_ClearsOverdueFlag() {
        savedPersonalCredit.setOutstandingBalance(new BigDecimal("500.00"));
        savedPersonalCredit.setIsOverdue(true);

        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));
        when(creditRepository.save(any(Credit.class))).thenReturn(savedPersonalCredit);

        creditService.applyPayment("crd001", new BigDecimal("500.00"));

        assertFalse(savedPersonalCredit.getIsOverdue());
        assertEquals(BigDecimal.ZERO, savedPersonalCredit.getOutstandingBalance()
                .subtract(new BigDecimal("500.00")).add(new BigDecimal("500.00"))
                .subtract(new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("Should throw exception when payment exceeds outstanding balance")
    void applyPayment_ExceedsOutstanding_ThrowsException() {
        savedPersonalCredit.setOutstandingBalance(new BigDecimal("100.00"));

        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));

        assertThrows(BusinessValidationException.class,
                () -> creditService.applyPayment("crd001",
                        new BigDecimal("9999.00")));
    }

    // ─── FIND TESTS ───────────────────────────────

    @Test
    @DisplayName("Should find credit by id successfully")
    void findCreditById_Success() {
        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));

        CreditResponseDto result = creditService.findCreditById("crd001");

        assertNotNull(result);
        assertEquals("crd001", result.getId());
    }

    @Test
    @DisplayName("Should throw exception when credit not found by id")
    void findCreditById_NotFound_ThrowsException() {
        when(creditRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(CreditNotFoundException.class,
                () -> creditService.findCreditById("nonexistent"));
    }

    @Test
    @DisplayName("Should return all credits for a customer")
    void findCreditsByCustomerId_Success() {
        when(creditRepository.findByCustomerId("customer123"))
                .thenReturn(List.of(savedPersonalCredit, savedCreditCard));

        List<CreditResponseDto> result = creditService.findCreditsByCustomerId("customer123");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should deactivate credit successfully")
    void deleteCredit_Success() {
        when(creditRepository.findById("crd001"))
                .thenReturn(Optional.of(savedPersonalCredit));
        when(creditRepository.save(any(Credit.class))).thenReturn(savedPersonalCredit);

        creditService.deleteCredit("crd001");

        verify(creditRepository, times(1)).save(any(Credit.class));
        assertFalse(savedPersonalCredit.getStatus());
    }
}