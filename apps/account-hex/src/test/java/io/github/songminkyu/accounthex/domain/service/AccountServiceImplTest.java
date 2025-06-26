package io.github.songminkyu.accounthex.domain.service;

import io.github.songminkyu.accounthex.domain.exception.CustomerAlreadyExistsException;
import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.mapper.CustomerMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.AccountsMsgDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;
import io.github.songminkyu.accounthex.domain.port.spi.AccountRepository;
import io.github.songminkyu.accounthex.domain.port.spi.CustomerRepository;
import io.github.songminkyu.accounthex.domain.port.spi.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MessageSender messageSender;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private CustomerMapper customerMapper;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                accountRepository,
                customerRepository,
                messageSender,
                accountMapper,
                customerMapper
        );
    }

    @Test
    void createAccount_ShouldCreateAccountAndSendCommunication() throws ExecutionException, InterruptedException {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(null, "Savings", "123 Main St")
        );

        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");
        customer.setMobileNumber("1234567890");

        Account account = new Account();
        account.setAccountNumber(1234567890L);
        account.setCustomerId(1L);
        account.setAccountType("Savings");
        account.setBranchAddress("123 Main St");
        account.setDeleted(false);

        when(customerRepository.existsByMobileNumber("1234567890")).thenReturn(false);
        when(customerMapper.toEntity(customerDTO)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(messageSender.sendCommunication(any(AccountsMsgDTO.class))).thenReturn(true);

        // Act
        CompletableFuture<Void> future = accountService.createAccount(customerDTO);
        future.get(); // Wait for completion

        // Assert
        verify(customerRepository).existsByMobileNumber("1234567890");
        verify(customerMapper).toEntity(customerDTO);
        verify(customerRepository).save(customer);
        verify(accountRepository).save(any(Account.class));
        verify(messageSender).sendCommunication(any(AccountsMsgDTO.class));
    }

    @Test
    void createAccount_ShouldThrowException_WhenCustomerAlreadyExists() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(null, "Savings", "123 Main St")
        );

        when(customerRepository.existsByMobileNumber("1234567890")).thenReturn(true);

        // Act & Assert
        assertThrows(CustomerAlreadyExistsException.class, () -> {
            accountService.createAccount(customerDTO);
        });

        verify(customerRepository).existsByMobileNumber("1234567890");
        verifyNoMoreInteractions(customerMapper, customerRepository, accountRepository, messageSender);
    }

    @Test
    void fetchAccount_ShouldReturnCustomerDTO() {
        // Arrange
        String mobileNumber = "1234567890";
        
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");
        customer.setMobileNumber(mobileNumber);
        
        Account account = new Account();
        account.setAccountNumber(1234567890L);
        account.setCustomerId(1L);
        account.setAccountType("Savings");
        account.setBranchAddress("123 Main St");
        
        CustomerDTO expectedCustomerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                mobileNumber,
                new AccountDTO(1234567890L, "Savings", "123 Main St")
        );
        
        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(account));
        when(customerMapper.toDto(customer, account)).thenReturn(expectedCustomerDTO);
        
        // Act
        CustomerDTO result = accountService.fetchAccount(mobileNumber);
        
        // Assert
        assertEquals(expectedCustomerDTO, result);
        verify(customerRepository).findByMobileNumber(mobileNumber);
        verify(accountRepository).findByCustomerId(1L);
        verify(customerMapper).toDto(customer, account);
    }
}