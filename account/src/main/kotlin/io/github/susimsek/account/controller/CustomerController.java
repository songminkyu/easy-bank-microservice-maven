package io.github.susimsek.account.controller;

import io.github.susimsek.account.dto.CustomerDetailsDTO;
import io.github.susimsek.account.dto.ErrorResponseDTO;
import io.github.susimsek.account.service.CustomerService;
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

@Slf4j
@Tag(
    name = "REST API for Customer in EazyBank",
    description = "REST APIs in EazyBank to FETCH customer.csv details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @Operation(
        summary = "Fetch Customer Details REST API",
        description = "REST API to fetch Customer details based on a mobile number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping("/customer-details")
    public ResponseEntity<CustomerDetailsDTO> fetchCustomerDetails(
        @RequestHeader("X-Correlation-Id") String correlationId,
        @RequestParam
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber) {
        log.debug("fetchCustomerDetails method start");
        var model = customerService.fetchCustomerDetails(mobileNumber, correlationId);
        log.debug("fetchCustomerDetails method end");
        return ResponseEntity.ok(model);
    }


}