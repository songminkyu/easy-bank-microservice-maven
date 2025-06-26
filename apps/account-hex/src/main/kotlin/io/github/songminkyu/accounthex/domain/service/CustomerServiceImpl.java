package io.github.songminkyu.accounthex.domain.service;

import io.github.songminkyu.accounthex.domain.exception.EntityNotFoundException;
import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.mapper.CustomerMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CardDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDetailsDTO;
import io.github.songminkyu.accounthex.domain.model.dto.LoanDTO;
import io.github.songminkyu.accounthex.domain.port.api.CustomerService;
import io.github.songminkyu.accounthex.domain.port.spi.AccountRepository;
import io.github.songminkyu.accounthex.domain.port.spi.CustomerRepository;
import io.github.songminkyu.accounthex.domain.port.spi.CardClient;
import io.github.songminkyu.accounthex.domain.port.spi.LoanClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of the CustomerService primary port.
 * This class contains the core business logic for customer operations.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardClient cardClient;
    private final LoanClient loanClient;
    private final AccountMapper accountMapper;

    @Override
    public CustomerDetailsDTO fetchCustomerDetails(String mobileNumber, String correlationId) {
        log.info("fetchCustomerDetails method start");
        
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found with mobile number: " + mobileNumber));
            
        Account account = accountRepository.findByCustomerId(customer.getCustomerId())
            .orElseThrow(() -> new EntityNotFoundException("Account not found for customer ID: " + customer.getCustomerId()));
            
        AccountDTO accountDTO = accountMapper.toDto(account);
        
        List<CardDTO> cards = fetchCards(mobileNumber, correlationId);
        List<LoanDTO> loans = fetchLoans(mobileNumber, correlationId);
        
        CustomerDetailsDTO customerDetails = CustomerDetailsDTO.builder()
            .name(customer.getName())
            .email(customer.getEmail())
            .mobileNumber(customer.getMobileNumber())
            .account(accountDTO)
            .cards(cards)
            .loans(loans)
            .build();
            
        log.info("fetchCustomerDetails method end");
        return customerDetails;
    }
    
    private List<CardDTO> fetchCards(String mobileNumber, String correlationId) {
        try {
            return cardClient.fetchCardDetails(mobileNumber, correlationId);
        } catch (Exception ex) {
            log.error("Error while fetching card details: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<LoanDTO> fetchLoans(String mobileNumber, String correlationId) {
        try {
            return loanClient.fetchLoanDetails(mobileNumber, correlationId);
        } catch (Exception ex) {
            log.error("Error while fetching loan details: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}