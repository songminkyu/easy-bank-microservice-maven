package io.github.songminkyu.account.controller;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.songminkyu.account.constants.AccountConstants;
import io.github.songminkyu.account.dto.AccountContactInfoDTO;
import io.github.songminkyu.account.dto.AccountDTO;
import io.github.songminkyu.account.dto.CustomerDTO;
import io.github.songminkyu.account.dto.ErrorResponseDTO;
import io.github.songminkyu.account.dto.ResponseDTO;
import io.github.songminkyu.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(
    name = "account",
    description = "CRUD REST APIs for Accounts in EazyBank"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class AccountController {

    private final AccountService accountService;

    private final Environment environment;

    private final AccountContactInfoDTO accountContactInfo;

    @Value("${build.version}")
    private String buildVersion;

    @GetMapping("/test")
    public String test() {
        accountService.test();
        return "test";
    }

    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Create Account REST API",
        description = "REST API to create new Customer &  Account inside EazyBank"
    )
    @ApiResponse(
        responseCode = "201",
        description = "Created"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @PostMapping("/account")
    public ResponseEntity<ResponseDTO> createAccount(@Valid @RequestBody CustomerDTO customer) {
        return accountService.createAccount(customer)
            .thenApply(ignore -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(AccountConstants.STATUS_201, AccountConstants.MESSAGE_201)))
            .join();
    }

    @Operation(
        summary = "Fetch Account Details REST API",
        description = "REST API to fetch Customer &  Account details based on a mobile number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not Found"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping("/account")
    public ResponseEntity<CustomerDTO> fetchAccountDetails(
        @RequestParam
        @NotBlank
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber) {
        var marker = LabelMarker.of("mobileNumber", () ->
            String.valueOf(mobileNumber));
        log.info(marker, "account successfully fetched");
        var model = accountService.fetchAccount(mobileNumber);
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Fetch Account Revisions REST API",
        description = "REST API to fetch Account revisions based on a account number"
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
    @GetMapping("/account/{accountNumber:\\d+}/revisions")
    public ResponseEntity<List<AccountDTO>> getAccountRevisions(
        @NotBlank
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        @PathVariable Long accountNumber) {
        var modelList = accountService.getAccountRevisions(accountNumber);
        return ResponseEntity.ok(modelList);
    }

    @Operation(
        summary = "Fetch Account Creator REST API",
        description = "REST API to fetch Account creator based on a account number"
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
    @GetMapping("/account/{accountNumber:\\d+}/creator")
    public ResponseEntity<String> getCreatorUsername(
        @NotBlank
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        @PathVariable Long accountNumber) {
        var model = accountService.geCreatorUsername(accountNumber);
        return ResponseEntity.ok(model);
    }

    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Update Account Details REST API",
        description = "REST API to update Customer &  Account details based on a account number"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not Found"
    )
    @ApiResponse(
        responseCode = "417",
        description = "Expectation Failed"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @PutMapping("/account/{accountNumber:^$|\\d{10}}")
    public ResponseEntity<ResponseDTO> updateAccountDetails(
        @NotNull
        @PathVariable Long accountNumber,
        @Valid @RequestBody CustomerDTO customer) {
        boolean isUpdated = accountService.updateAccount(accountNumber, customer);
        if (isUpdated) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(AccountConstants.STATUS_200, AccountConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                .status(HttpStatus.EXPECTATION_FAILED)
                .body(new ResponseDTO(AccountConstants.STATUS_417, AccountConstants.MESSAGE_417_UPDATE));
        }
    }


    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Delete Account & Customer Details REST API",
        description = "REST API to delete Customer &  Account details based on a mobile number"
    )
    @ApiResponse(
        responseCode = "204",
        description = "No content"
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not Found"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccountDetails(
        @RequestParam
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber
    ) {
        accountService.deleteAccount(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get Build information",
        description = "Get Build information that is deployed into account microservice"
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
    @Retry(name = "getBuildInfo", fallbackMethod = "getBuildInfoFallback")
    @GetMapping("/build-info")
    public ResponseEntity<String> getBuildInfo() throws TimeoutException {
        log.debug("getBuildInfo() method Invoked");
        return ResponseEntity
            .ok(buildVersion);
    }

    public ResponseEntity<String> getBuildInfoFallback() {
        log.debug("getBuildInfoFallback() method Invoked");
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("0.9");
    }


    @Operation(
        summary = "Get Java version",
        description = "Get Java versions details that is installed into account microservice"
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
    @RateLimiter(name = "getJavaVersion", fallbackMethod = "getJavaVersionFallback")
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(environment.getProperty("JAVA_HOME"));
    }

    public ResponseEntity<String> getJavaVersionFallback() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Java 17");
    }

    @Operation(
        summary = "Get Contact Info",
        description = "Contact Info details that can be reached out in case of any issues"
    )
    @ApiResponse(responseCode = "200", description = "Ok")
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(
            schema = @Schema(implementation = ErrorResponseDTO.class)
        )
    )
    @GetMapping("/contact-info")
    public ResponseEntity<AccountContactInfoDTO> getContactInfo() {
        return ResponseEntity.ok(accountContactInfo);
    }

}