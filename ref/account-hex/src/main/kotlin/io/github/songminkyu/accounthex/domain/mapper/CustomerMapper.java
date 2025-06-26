package io.github.songminkyu.accounthex.domain.mapper;

import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;

/**
 * Mapper for converting between Customer domain entities and CustomerDTO data transfer objects.
 */
public interface CustomerMapper {

    /**
     * Converts a Customer entity to a CustomerDTO.
     *
     * @param customer the customer entity
     * @return the customer DTO
     */
    CustomerDTO toDto(Customer customer);

    /**
     * Converts a Customer entity and Account entity to a CustomerDTO.
     *
     * @param customer the customer entity
     * @param account the account entity
     * @return the customer DTO with account information
     */
    CustomerDTO toDto(Customer customer, Account account);

    /**
     * Converts a CustomerDTO to a Customer entity.
     *
     * @param customerDTO the customer DTO
     * @return the customer entity
     */
    Customer toEntity(CustomerDTO customerDTO);

    /**
     * Updates a Customer entity from a CustomerDTO.
     *
     * @param customer the customer entity to update
     * @param customerDTO the customer DTO with updated values
     */
    void updateEntityFromDto(Customer customer, CustomerDTO customerDTO);
}