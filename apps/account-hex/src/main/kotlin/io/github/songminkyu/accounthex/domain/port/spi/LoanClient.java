package io.github.songminkyu.accounthex.domain.port.spi;

import io.github.songminkyu.accounthex.domain.model.dto.LoanDTO;

import java.util.List;

/**
 * Secondary port for loan operations.
 * This interface defines the operations for retrieving loan information from external systems.
 */
public interface LoanClient {

    /**
     * Fetches loan details for a customer.
     *
     * @param mobileNumber the mobile number of the customer
     * @param correlationId the correlation ID for tracing
     * @return a list of loan DTOs
     */
    List<LoanDTO> fetchLoanDetails(String mobileNumber, String correlationId);
}