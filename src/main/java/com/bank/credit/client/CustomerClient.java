package com.bank.credit.client;

import com.bank.credit.exception.BusinessValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP client for communicating with the ms-customer microservice.
 * Used to validate customer existence and retrieve customer type.
 */
@Slf4j
@Component
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    /**
     * Constructor injection of RestTemplate and config URL.
     *
     * @param restTemplate       the HTTP client
     * @param customerServiceUrl base URL for ms-customer from config
     */
    public CustomerClient(RestTemplate restTemplate,
                          @Value("${services.customer.url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    /**
     * Retrieves customer data by ID from ms-customer.
     *
     * @param customerId the customer ID to look up
     * @return a Map containing the customer's data fields
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> findCustomerById(String customerId) {
        try {
            log.info("Calling ms-customer for customerId: {}", customerId);
            return restTemplate.getForObject(
                    customerServiceUrl + "/api/v1/customers/" + customerId,
                    Map.class
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Customer not found with id: {}", customerId);
            throw new BusinessValidationException(
                    "Customer not found with id: " + customerId);
        } catch (Exception ex) {
            log.error("Error calling ms-customer: {}", ex.getMessage());
            throw new BusinessValidationException(
                    "Error communicating with customer service");
        }
    }
}