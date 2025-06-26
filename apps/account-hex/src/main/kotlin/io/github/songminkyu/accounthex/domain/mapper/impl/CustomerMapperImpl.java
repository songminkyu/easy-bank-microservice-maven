package io.github.songminkyu.accounthex.domain.mapper.impl;

import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.mapper.CustomerMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;

/**
 * Implementation of the CustomerMapper interface.
 * This class provides methods for converting between Customer domain entities and CustomerDTO data transfer objects.
 * This is a pure domain implementation without any framework-specific annotations.
 */
public class CustomerMapperImpl implements CustomerMapper {

    private final AccountMapper accountMapper;

    public CustomerMapperImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public CustomerDTO toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new CustomerDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getMobileNumber(),
                null
        );
    }

    @Override
    public CustomerDTO toDto(Customer customer, Account account) {
        if (customer == null) {
            return null;
        }

        return new CustomerDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getMobileNumber(),
                account != null ? accountMapper.toDto(account) : null
        );
    }

    @Override
    public Customer toEntity(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setName(customerDTO.name());
        customer.setEmail(customerDTO.email());
        customer.setMobileNumber(customerDTO.mobileNumber());
        return customer;
    }

    @Override
    public void updateEntityFromDto(Customer customer, CustomerDTO customerDTO) {
        if (customerDTO == null) {
            return;
        }

        if (customerDTO.name() != null) {
            customer.setName(customerDTO.name());
        }
        if (customerDTO.email() != null) {
            customer.setEmail(customerDTO.email());
        }
        if (customerDTO.mobileNumber() != null) {
            customer.setMobileNumber(customerDTO.mobileNumber());
        }
    }
}
