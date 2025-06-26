package io.github.songminkyu.accounthex.adapter.in.web;

import io.github.songminkyu.accounthex.domain.exception.EntityNotFoundException;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CardDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDetailsDTO;
import io.github.songminkyu.accounthex.domain.model.dto.LoanDTO;
import io.github.songminkyu.accounthex.domain.port.api.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void fetchCustomerDetails_ShouldReturnCustomerDetails() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

        CustomerDetailsDTO customerDetails = CustomerDetailsDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .mobileNumber(mobileNumber)
                .account(new AccountDTO(1234567890L, "Savings", "123 Main St"))
                .cards(Arrays.asList(
                        new CardDTO("1234567890", "1234-5678-9012-3456", "Credit Card", 10000, 1000, 9000)
                ))
                .loans(Arrays.asList(
                        new LoanDTO("1234567890", "L123456", "Home Loan", 100000, 10000, 90000)
                ))
                .build();

        when(customerService.fetchCustomerDetails(mobileNumber, correlationId)).thenReturn(customerDetails);

        // Act & Assert
        mockMvc.perform(get("/api/customers/details")
                .header("X-Correlation-Id", correlationId)
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.mobileNumber", is(mobileNumber)))
                .andExpect(jsonPath("$.account.accountNumber", is(1234567890)))
                .andExpect(jsonPath("$.account.accountType", is("Savings")))
                .andExpect(jsonPath("$.account.branchAddress", is("123 Main St")))
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].mobileNumber", is(mobileNumber)))
                .andExpect(jsonPath("$.cards[0].cardNumber", is("1234-5678-9012-3456")))
                .andExpect(jsonPath("$.cards[0].cardType", is("Credit Card")))
                .andExpect(jsonPath("$.cards[0].totalLimit", is(10000)))
                .andExpect(jsonPath("$.cards[0].amountUsed", is(1000)))
                .andExpect(jsonPath("$.cards[0].availableAmount", is(9000)))
                .andExpect(jsonPath("$.loans", hasSize(1)))
                .andExpect(jsonPath("$.loans[0].mobileNumber", is(mobileNumber)))
                .andExpect(jsonPath("$.loans[0].loanNumber", is("L123456")))
                .andExpect(jsonPath("$.loans[0].loanType", is("Home Loan")))
                .andExpect(jsonPath("$.loans[0].totalLoan", is(100000)))
                .andExpect(jsonPath("$.loans[0].amountPaid", is(10000)))
                .andExpect(jsonPath("$.loans[0].outstandingAmount", is(90000)));

        verify(customerService).fetchCustomerDetails(mobileNumber, correlationId);
    }

    @Test
    void fetchCustomerDetails_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

        when(customerService.fetchCustomerDetails(mobileNumber, correlationId))
                .thenThrow(new EntityNotFoundException("Customer not found with mobile number: " + mobileNumber));

        // Act & Assert
        mockMvc.perform(get("/api/customers/details")
                .header("X-Correlation-Id", correlationId)
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isNotFound());

        verify(customerService).fetchCustomerDetails(mobileNumber, correlationId);
    }

    @Test
    void fetchCustomerDetails_ShouldReturnBadRequest_WhenMobileNumberIsInvalid() throws Exception {
        // Arrange
        String mobileNumber = "123"; // Invalid mobile number
        String correlationId = "test-correlation-id";

        // Act & Assert
        mockMvc.perform(get("/api/customers/details")
                .header("X-Correlation-Id", correlationId)
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchCustomerDetails_ShouldReturnCustomerDetailsWithEmptyCardsAndLoans() throws Exception {
        // Arrange
        String mobileNumber = "1234567890";
        String correlationId = "test-correlation-id";

        CustomerDetailsDTO customerDetails = CustomerDetailsDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .mobileNumber(mobileNumber)
                .account(new AccountDTO(1234567890L, "Savings", "123 Main St"))
                .cards(Collections.emptyList())
                .loans(Collections.emptyList())
                .build();

        when(customerService.fetchCustomerDetails(mobileNumber, correlationId)).thenReturn(customerDetails);

        // Act & Assert
        mockMvc.perform(get("/api/customers/details")
                .header("X-Correlation-Id", correlationId)
                .param("mobileNumber", mobileNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.mobileNumber", is(mobileNumber)))
                .andExpect(jsonPath("$.account.accountNumber", is(1234567890)))
                .andExpect(jsonPath("$.cards", hasSize(0)))
                .andExpect(jsonPath("$.loans", hasSize(0)));

        verify(customerService).fetchCustomerDetails(mobileNumber, correlationId);
    }
}