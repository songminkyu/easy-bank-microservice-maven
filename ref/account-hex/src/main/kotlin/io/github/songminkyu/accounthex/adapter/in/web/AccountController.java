package io.github.songminkyu.accounthex.adapter.in.web;

import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;
import io.github.songminkyu.accounthex.domain.port.api.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for account operations.
 * This is a primary adapter that exposes the AccountService to the outside world.
 */
@Tag(
    name = "REST API for Accounts in EazyBank",
    description = "REST APIs in EazyBank to CREATE, UPDATE, FETCH AND DELETE account details"
)
@RestController
@RequestMapping(path = "/api/accounts", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @Operation(
        summary = "Create Account REST API",
        description = "REST API to create new Customer & Account inside EazyBank"
    )
    @ApiResponse(
        responseCode = "201",
        description = "HTTP Status CREATED"
    )
    @PostMapping
    public ResponseEntity<Void> createAccount(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("Creating account for customer: {}", customerDTO.name());
        CompletableFuture<Void> future = accountService.createAccount(customerDTO);
        future.join(); // Wait for the operation to complete
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
        summary = "Fetch Account Details REST API",
        description = "REST API to fetch account details based on a mobile number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @GetMapping
    public ResponseEntity<CustomerDTO> fetchAccountDetails(
            @RequestParam
            @Pattern(regexp = "(^$|\\d{10})", message = "Mobile number must be 10 digits")
            String mobileNumber) {
        log.info("Fetching account details for mobile number: {}", mobileNumber);
        CustomerDTO customerDTO = accountService.fetchAccount(mobileNumber);
        return ResponseEntity.ok(customerDTO);
    }

    @Operation(
        summary = "Update Account Details REST API",
        description = "REST API to update account details based on account number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @ApiResponse(
        responseCode = "417",
        description = "Expectation Failed"
    )
    @PutMapping("/{accountNumber}")
    public ResponseEntity<Boolean> updateAccountDetails(
            @PathVariable Long accountNumber,
            @Valid @RequestBody CustomerDTO customerDTO) {
        log.info("Updating account details for account number: {}", accountNumber);
        boolean isUpdated = accountService.updateAccount(accountNumber, customerDTO);
        if (isUpdated) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(false);
        }
    }

    @Operation(
        summary = "Delete Account REST API",
        description = "REST API to delete account & customer details based on mobile number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @ApiResponse(
        responseCode = "417",
        description = "Expectation Failed"
    )
    @DeleteMapping("/{mobileNumber}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable
            @Pattern(regexp = "(^$|\\d{10})", message = "Mobile number must be 10 digits")
            String mobileNumber) {
        log.info("Deleting account for mobile number: {}", mobileNumber);
        accountService.deleteAccount(mobileNumber);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get Account Revisions REST API",
        description = "REST API to get all revisions of an account"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @GetMapping("/{accountNumber}/revisions")
    public ResponseEntity<List<AccountDTO>> getAccountRevisions(@PathVariable Long accountNumber) {
        log.info("Getting revisions for account number: {}", accountNumber);
        List<AccountDTO> revisions = accountService.getAccountRevisions(accountNumber);
        return ResponseEntity.ok(revisions);
    }

    @Operation(
        summary = "Get Account Creator REST API",
        description = "REST API to get the username of the creator of an account"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @GetMapping("/{accountNumber}/creator")
    public ResponseEntity<String> getAccountCreator(@PathVariable Long accountNumber) {
        log.info("Getting creator for account number: {}", accountNumber);
        String creatorUsername = accountService.getCreatorUsername(accountNumber);
        return ResponseEntity.ok(creatorUsername);
    }

    @Operation(
        summary = "Update Communication Status REST API",
        description = "REST API to update communication status of an account"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @ApiResponse(
        responseCode = "417",
        description = "Expectation Failed"
    )
    @PutMapping("/{accountNumber}/communication")
    public ResponseEntity<Boolean> updateCommunicationStatus(@PathVariable Long accountNumber) {
        log.info("Updating communication status for account number: {}", accountNumber);
        boolean isUpdated = accountService.updateCommunicationStatus(accountNumber);
        if (isUpdated) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(false);
        }
    }
}