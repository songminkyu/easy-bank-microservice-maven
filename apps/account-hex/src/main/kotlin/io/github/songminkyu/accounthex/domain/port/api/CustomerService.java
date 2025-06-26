package io.github.songminkyu.accounthex.domain.port.api;

import io.github.songminkyu.accounthex.domain.model.dto.CustomerDetailsDTO;

/**
 * Primary port for customer operations.
 * This interface defines the operations that can be performed on customers.
 */
public interface CustomerService {

    /**
     * Fetches detailed customer information including related services data.
     *
     * @param mobileNumber the mobile number of the customer
     * @param correlationId the correlation ID for tracing
     * @return the customer details including related services information
     */
    CustomerDetailsDTO fetchCustomerDetails(String mobileNumber, String correlationId);
}