package io.github.songminkyu.accounthex.adapter.in.web;

import io.github.songminkyu.accounthex.domain.model.dto.CustomerDetailsDTO;
import io.github.songminkyu.accounthex.domain.port.api.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for customer operations.
 * This is a primary adapter that exposes the CustomerService to the outside world.
 */
@Tag(
    name = "REST API for Customer in EazyBank",
    description = "REST APIs in EazyBank to FETCH customer details"
)
@RestController
@RequestMapping(path = "/api/customers", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
        summary = "Fetch Customer Details REST API",
        description = "REST API to fetch Customer details based on a mobile number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "HTTP Status OK"
    )
    @GetMapping("/details")
    public ResponseEntity<CustomerDetailsDTO> fetchCustomerDetails(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestParam
            @Pattern(regexp = "(^$|\\d{10})", message = "Mobile number must be 10 digits")
            String mobileNumber) {
        log.info("fetchCustomerDetails method start with correlationId: {}", correlationId);
        CustomerDetailsDTO customerDetails = customerService.fetchCustomerDetails(mobileNumber, correlationId);
        log.info("fetchCustomerDetails method end");
        return ResponseEntity.ok(customerDetails);
    }
}