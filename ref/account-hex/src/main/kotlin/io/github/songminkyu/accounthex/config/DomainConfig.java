package io.github.songminkyu.accounthex.config;

import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.mapper.CustomerMapper;
import io.github.songminkyu.accounthex.domain.port.api.AccountService;
import io.github.songminkyu.accounthex.domain.port.api.CustomerService;
import io.github.songminkyu.accounthex.domain.port.repository.AccountRepository;
import io.github.songminkyu.accounthex.domain.port.spi.CardClient;
import io.github.songminkyu.accounthex.domain.port.repository.CustomerRepository;
import io.github.songminkyu.accounthex.domain.port.spi.LoanClient;
import io.github.songminkyu.accounthex.domain.port.spi.MessageSender;
import io.github.songminkyu.accounthex.domain.service.AccountServiceImpl;
import io.github.songminkyu.accounthex.domain.service.CustomerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the domain layer.
 * This class configures the domain service implementations.
 */
@Configuration
public class DomainConfig {

    @Bean
    public AccountService accountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            MessageSender messageSender,
            AccountMapper accountMapper,
            CustomerMapper customerMapper) {
        return new AccountServiceImpl(
                accountRepository,
                customerRepository,
                messageSender,
                accountMapper,
                customerMapper);
    }

    @Bean
    public CustomerService customerService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            CardClient cardClient,
            LoanClient loanClient,
            AccountMapper accountMapper) {
        return new CustomerServiceImpl(
                customerRepository,
                accountRepository,
                cardClient,
                loanClient,
                accountMapper);
    }
}