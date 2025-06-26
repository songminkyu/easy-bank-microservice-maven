package io.github.songminkyu.accounthex.domain.port.spi;

import io.github.songminkyu.accounthex.domain.model.dto.CardDTO;

import java.util.List;

/**
 * Secondary port for card operations.
 * This interface defines the operations for retrieving card information from external systems.
 */
public interface CardClient {

    /**
     * Fetches card details for a customer.
     *
     * @param mobileNumber the mobile number of the customer
     * @param correlationId the correlation ID for tracing
     * @return a list of card DTOs
     */
    List<CardDTO> fetchCardDetails(String mobileNumber, String correlationId);
}