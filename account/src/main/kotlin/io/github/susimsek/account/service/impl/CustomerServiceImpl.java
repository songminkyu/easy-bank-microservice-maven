package io.github.susimsek.account.service.impl;

import io.github.susimsek.account.client.CardGraphQlClient;
import io.github.susimsek.account.client.LoanFeignClient;
import io.github.susimsek.account.dto.CustomerDetailsDTO;
import io.github.susimsek.account.entity.Account;
import io.github.susimsek.account.entity.Customer;
import io.github.susimsek.account.exception.EntityNotFoundException;
import io.github.susimsek.account.mapper.CustomerMapper;
import io.github.susimsek.account.repository.AccountRepository;
import io.github.susimsek.account.repository.CustomerRepository;
import io.github.susimsek.account.service.CustomerService;
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
