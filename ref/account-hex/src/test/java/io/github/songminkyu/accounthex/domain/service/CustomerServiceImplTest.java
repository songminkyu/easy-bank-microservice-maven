package io.github.songminkyu.accounthex.domain.service;

import io.github.songminkyu.accounthex.domain.exception.EntityNotFoundException;
import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.Customer;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CardDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDetailsDTO;
import io.github.songminkyu.accounthex.domain.model.dto.LoanDTO;
import io.github.songminkyu.accounthex.domain.port.repository.AccountRepository;
import io.github.songminkyu.accounthex.domain.port.spi.CardClient;
import io.github.songminkyu.accounthex.domain.port.repository.CustomerRepository;
import io.github.songminkyu.accounthex.domain.port.spi.LoanClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardClient cardClient;

    @Mock
    private LoanClient loanClient;

    @Mock
    private AccountMapper accountMapper;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(
                customerRepository,
                accountRepository,
                cardClient,
                loanClient,
                accountMapper
        );
    }

    @Test
    void fetchCustomerDetails_ShouldReturnCustomerDetails() {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

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

        AccountDTO accountDTO = new AccountDTO(1234567890L, "Savings", "123 Main St");

        List<CardDTO> cards = Arrays.asList(
                new CardDTO("1234567890", "1234-5678-9012-3456", "Credit Card", 10000, 1000, 9000)
        );

        List<LoanDTO> loans = Arrays.asList(
                new LoanDTO("1234567890", "L123456", "Home Loan", 100000, 10000, 90000)
        );

        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDTO);
        when(cardClient.fetchCardDetails(mobileNumber, correlationId)).thenReturn(cards);
        when(loanClient.fetchLoanDetails(mobileNumber, correlationId)).thenReturn(loans);

        // Act
        CustomerDetailsDTO result = customerService.fetchCustomerDetails(mobileNumber, correlationId);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(mobileNumber, result.getMobileNumber());
        assertEquals(accountDTO, result.getAccount());
        assertEquals(cards, result.getCards());
        assertEquals(loans, result.getLoans());

        verify(customerRepository).findByMobileNumber(mobileNumber);
        verify(accountRepository).findByCustomerId(1L);
        verify(accountMapper).toDto(account);
        verify(cardClient).fetchCardDetails(mobileNumber, correlationId);
        verify(loanClient).fetchLoanDetails(mobileNumber, correlationId);
    }

    @Test
    void fetchCustomerDetails_ShouldHandleCardClientException() {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

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

        AccountDTO accountDTO = new AccountDTO(1234567890L, "Savings", "123 Main St");

        List<LoanDTO> loans = Arrays.asList(
                new LoanDTO("1234567890", "L123456", "Home Loan", 100000, 10000, 90000)
        );

        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDTO);
        when(cardClient.fetchCardDetails(mobileNumber, correlationId)).thenThrow(new RuntimeException("Card service unavailable"));
        when(loanClient.fetchLoanDetails(mobileNumber, correlationId)).thenReturn(loans);

        // Act
        CustomerDetailsDTO result = customerService.fetchCustomerDetails(mobileNumber, correlationId);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(mobileNumber, result.getMobileNumber());
        assertEquals(accountDTO, result.getAccount());
        assertEquals(Collections.emptyList(), result.getCards());
        assertEquals(loans, result.getLoans());

        verify(customerRepository).findByMobileNumber(mobileNumber);
        verify(accountRepository).findByCustomerId(1L);
        verify(accountMapper).toDto(account);
        verify(cardClient).fetchCardDetails(mobileNumber, correlationId);
        verify(loanClient).fetchLoanDetails(mobileNumber, correlationId);
    }

    @Test
    void fetchCustomerDetails_ShouldThrowException_WhenCustomerNotFound() {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

        when(customerRepository.findByMobileNumber(mobileNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            customerService.fetchCustomerDetails(mobileNumber, correlationId);
        });

        verify(customerRepository).findByMobileNumber(mobileNumber);
        verifyNoInteractions(accountRepository, accountMapper, cardClient, loanClient);
    }
}