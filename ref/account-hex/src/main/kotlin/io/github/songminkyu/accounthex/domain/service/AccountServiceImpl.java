package io.github.songminkyu.accounthex.domain.service;

import io.github.songminkyu.accounthex.domain.exception.CustomerAlreadyExistsException;
import io.github.songminkyu.accounthex.domain.exception.EntityNotFoundException;
import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.mapper.CustomerMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.AccountsMsgDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;
import io.github.songminkyu.accounthex.domain.port.api.AccountService;
import io.github.songminkyu.accounthex.domain.port.repository.AccountRepository;
import io.github.songminkyu.accounthex.domain.port.repository.CustomerRepository;
import io.github.songminkyu.accounthex.domain.port.spi.MessageSender;
import io.github.songminkyu.accounthex.domain.port.spi.DomainLogger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of the AccountService primary port.
 * This class contains the core business logic for account operations.
 * This is a pure domain implementation without any framework-specific annotations.
 */
public class AccountServiceImpl implements AccountService {

    private static final Random RANDOM = new Random();
    private static final String SAVINGS = "Savings";
    private static final String ADDRESS = "123 Main Street, New York";

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final MessageSender messageSender;
    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final DomainLogger logger;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            MessageSender messageSender,
            AccountMapper accountMapper,
            CustomerMapper customerMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.messageSender = messageSender;
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.logger = message -> {}; // No-op logger as default
    }

    public AccountServiceImpl(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            MessageSender messageSender,
            AccountMapper accountMapper,
            CustomerMapper customerMapper,
            DomainLogger logger) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.messageSender = messageSender;
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Void> createAccount(CustomerDTO customerDTO) {
        if (customerRepository.existsByMobileNumber(customerDTO.mobileNumber())) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                + customerDTO.mobileNumber());
        }

        // Convert DTO to domain entity
        Customer customer = customerMapper.toEntity(customerDTO);

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);

        // Create and save account
        return CompletableFuture.completedFuture(
            accountRepository.save(createNewAccount(savedCustomer)))
            .thenAccept(savedAccount -> sendCommunication(savedAccount, savedCustomer));
    }

    @Override
    public CustomerDTO fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found with mobile number: " + mobileNumber));

        Account account = accountRepository.findByCustomerId(customer.getCustomerId())
            .orElseThrow(() -> new EntityNotFoundException("Account not found for customer ID: " + customer.getCustomerId()));

        return customerMapper.toDto(customer, account);
    }

    @Override
    public List<AccountDTO> getAccountRevisions(Long accountNumber) {
        return accountRepository.findRevisions(accountNumber)
            .stream()
            .map(revision -> accountMapper.toDto(revision.getEntity()))
            .collect(Collectors.toList());
    }

    @Override
    public String getCreatorUsername(Long accountNumber) {
        AccountRepository.Revision<Account> revision = accountRepository.findRevision(accountNumber, 1)
            .orElseThrow(() -> new EntityNotFoundException("Revision not found for account number: " + accountNumber));

        AccountRepository.RevisionMetadata metadata = revision.getMetadata();
        Object revisionEntity = metadata.getDelegate();

        // This would need to be adapted based on the actual implementation
        return revisionEntity.toString();
    }

    @Override
    public boolean updateAccount(Long accountNumber, CustomerDTO customerDTO) {
        boolean isUpdated = false;
        AccountDTO accountDTO = customerDTO.account();

        if (accountDTO != null) {
            Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with account number: " + accountNumber));

            // Update account fields
            accountMapper.updateEntityFromDto(account, accountDTO);
            account.setAccountNumber(accountNumber);
            accountRepository.save(account);

            // Update customer fields
            Customer customer = customerRepository.findById(account.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + account.getCustomerId()));

            customerMapper.updateEntityFromDto(customer, customerDTO);
            customerRepository.save(customer);

            isUpdated = true;
        }

        return isUpdated;
    }

    @Override
    public void deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found with mobile number: " + mobileNumber));

        accountRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
    }

    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        if (accountNumber == null) {
            return false;
        }

        Account account = accountRepository.findById(accountNumber)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with account number: " + accountNumber));

        account.setCommunicationSw(true);
        accountRepository.save(account);

        return true;
    }

    private Account createNewAccount(Customer customer) {
        Account account = new Account();
        account.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + RANDOM.nextInt(900000000);
        account.setAccountNumber(randomAccNumber);
        account.setAccountType(SAVINGS);
        account.setBranchAddress(ADDRESS);
        account.setDeleted(false);
        return account;
    }

    private void sendCommunication(Account account, Customer customer) {
        AccountsMsgDTO accountsMsgDTO = new AccountsMsgDTO(
            account.getAccountNumber(),
            customer.getName(),
            customer.getEmail(),
            customer.getMobileNumber()
        );

        log.info("Sending Communication request for the details: {}", accountsMsgDTO);
        boolean result = messageSender.sendCommunication(accountsMsgDTO);
        log.info("Is the Communication request successfully triggered? : {}", result);
    }
}
