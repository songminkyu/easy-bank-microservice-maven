package io.github.songminkyu.accounthex.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.songminkyu.accounthex.domain.exception.CustomerAlreadyExistsException;
import io.github.songminkyu.accounthex.domain.exception.EntityNotFoundException;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;
import io.github.songminkyu.accounthex.domain.port.api.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void createAccount_ShouldReturnCreated() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(null, "Savings", "123 Main St")
        );

        when(accountService.createAccount(any(CustomerDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated());

        verify(accountService).createAccount(any(CustomerDTO.class));
    }

    @Test
    void createAccount_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO(
                "John", // Name too short
                "invalid-email", // Invalid email
                "123", // Invalid mobile number
                new AccountDTO(null, "", "") // Empty required fields
        );

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void createAccount_ShouldReturnConflict_WhenCustomerAlreadyExists() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(null, "Savings", "123 Main St")
        );

        when(accountService.createAccount(any(CustomerDTO.class)))
                .thenThrow(new CustomerAlreadyExistsException("Customer already exists with mobile number: 1234567890"));

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isConflict());

        verify(accountService).createAccount(any(CustomerDTO.class));
    }

    @Test
    void fetchAccountDetails_ShouldReturnCustomerDTO() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                mobileNumber,
                new AccountDTO(1234567890L, "Savings", "123 Main St")
        );

        when(accountService.fetchAccount(mobileNumber)).thenReturn(customerDTO);

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.mobileNumber", is(mobileNumber)))
                .andExpect(jsonPath("$.account.accountNumber", is(1234567890)))
                .andExpect(jsonPath("$.account.accountType", is("Savings")))
                .andExpect(jsonPath("$.account.branchAddress", is("123 Main St")));

        verify(accountService).fetchAccount(mobileNumber);
    }

    @Test
    void fetchAccountDetails_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";

        when(accountService.fetchAccount(mobileNumber))
                .thenThrow(new EntityNotFoundException("Customer not found with mobile number: " + mobileNumber));

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isNotFound());

        verify(accountService).fetchAccount(mobileNumber);
    }

    @Test
    void updateAccountDetails_ShouldReturnTrue_WhenUpdateSucceeds() throws Exception {
        // Arrange
        Long accountNumber = 1234567890L;
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(accountNumber, "Checking", "456 Main St")
        );

        when(accountService.updateAccount(eq(accountNumber), any(CustomerDTO.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(accountService).updateAccount(eq(accountNumber), any(CustomerDTO.class));
    }

    @Test
    void updateAccountDetails_ShouldReturnExpectationFailed_WhenUpdateFails() throws Exception {
        // Arrange
        Long accountNumber = 1234567890L;
        CustomerDTO customerDTO = new CustomerDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                new AccountDTO(accountNumber, "Checking", "456 Main St")
        );

        when(accountService.updateAccount(eq(accountNumber), any(CustomerDTO.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isExpectationFailed())
                .andExpect(content().string("false"));

        verify(accountService).updateAccount(eq(accountNumber), any(CustomerDTO.class));
    }

    @Test
    void deleteAccount_ShouldReturnOk() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";

        doNothing().when(accountService).deleteAccount(mobileNumber);

        // Act & Assert
        mockMvc.perform(delete("/api/accounts/{mobileNumber}", mobileNumber))
                .andExpect(status().isOk());

        verify(accountService).deleteAccount(mobileNumber);
    }

    @Test
    void getAccountRevisions_ShouldReturnListOfRevisions() throws Exception {
        // Arrange
        Long accountNumber = 1234567890L;
        List<AccountDTO> revisions = Arrays.asList(
                new AccountDTO(accountNumber, "Savings", "123 Main St"),
                new AccountDTO(accountNumber, "Checking", "456 Main St")
        );

        when(accountService.getAccountRevisions(accountNumber)).thenReturn(revisions);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/revisions", accountNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber", is(1234567890)))
                .andExpect(jsonPath("$[0].accountType", is("Savings")))
                .andExpect(jsonPath("$[0].branchAddress", is("123 Main St")))
                .andExpect(jsonPath("$[1].accountNumber", is(1234567890)))
                .andExpect(jsonPath("$[1].accountType", is("Checking")))
                .andExpect(jsonPath("$[1].branchAddress", is("456 Main St")));

        verify(accountService).getAccountRevisions(accountNumber);
    }
}