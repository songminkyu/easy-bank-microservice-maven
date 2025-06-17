package io.github.songminkyu.account.service.impl;

import io.github.songminkyu.account.client.CardGraphQlClient;
import io.github.songminkyu.account.client.LoanFeignClient;
import io.github.songminkyu.account.dto.CustomerDetailsDTO;
import io.github.songminkyu.account.entity.Account;
import io.github.songminkyu.account.entity.Customer;
import io.github.songminkyu.account.exception.EntityNotFoundException;
import io.github.songminkyu.account.mapper.CustomerMapper;
import io.github.songminkyu.account.repository.AccountRepository;
import io.github.songminkyu.account.repository.CustomerRepository;
import io.github.songminkyu.account.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardGraphQlClient cardGraphQlClient;
    private final LoanFeignClient loanFeignClient;

    private final CustomerMapper customerMapper;

    @Override
    public CustomerDetailsDTO fetchCustomerDetails(String mobileNumber, String correlationId) {
        var customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Customer.class, "mobileNumber", mobileNumber)
        );
        var account = accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
            () -> new EntityNotFoundException(Account.class, "customerId", customer.getCustomerId().toString())
        );

        var loan = loanFeignClient.fetchLoanDetails(correlationId, mobileNumber);

        var card = cardGraphQlClient.fetchCardDetails(mobileNumber);

        return customerMapper.toCustomerDetailsDTO(customer, account, loan, card);
    }
}
