package io.github.susimsek.card.controller;

import com.github.loki4j.slf4j.marker.LabelMarker;
import io.github.susimsek.card.constants.CardConstants;
import io.github.susimsek.card.dto.CardContactInfoDTO;
import io.github.susimsek.card.dto.CardDTO;
import io.github.susimsek.card.dto.ErrorResponseDTO;
import io.github.susimsek.card.dto.ResponseDTO;
import io.github.susimsek.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(
    name = "card",
    description = "CRUD REST APIs for Cards in EazyBank"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class CardRestController {

    private final CardService cardService;

    private final Environment environment;

    private final CardContactInfoDTO cardContactInfo;

    @Value("${build.version}")
    private String buildVersion;

    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Create Card REST API",
        description = "REST API to create new Card inside EazyBank"
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
    @PostMapping("/card")
    public ResponseEntity<CardDTO> createCard(
        @RequestParam
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber) {
        var cardDTO = cardService.createCard(mobileNumber);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cardDTO);
    }

    @Operation(
        summary = "Fetch Card Details REST API",
        description = "REST API to fetch card details based on a mobile number"
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
    @GetMapping("/card")
    public ResponseEntity<CardDTO> fetchCardDetails(
        @RequestHeader("X-Correlation-Id") String correlationId,
        @RequestParam
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber) {
        var marker = LabelMarker.of("mobileNumber", () ->
            String.valueOf(mobileNumber));
        log.info(marker, "card successfully fetched");
        log.debug("fetchCardDetails method start");
        var model = cardService.getCard(mobileNumber);
        log.debug("fetchCardDetails method end");
        return ResponseEntity.ok(model);
    }

    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Update Card Details REST API",
        description = "REST API to update card details based on a card number"
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
    @PutMapping("/card/{cardNumber}")
    public ResponseEntity<ResponseDTO> updateCardDetails(
        @NotEmpty(message = "Card Number can not be a null or empty")
        @Pattern(regexp = "(^$|\\d{12})", message = "{jakarta.validation.constraint.CardNumber.Pattern.message}")
        @PathVariable String cardNumber,
        @Valid @RequestBody CardDTO card) {
        boolean isUpdated = cardService.updateCard(cardNumber, card);
        if (isUpdated) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDTO(CardConstants.STATUS_200, CardConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                .status(HttpStatus.EXPECTATION_FAILED)
                .body(new ResponseDTO(CardConstants.STATUS_417, CardConstants.MESSAGE_417_UPDATE));
        }
    }


    @SecurityRequirement(name = "auth")
    @Operation(
        summary = "Delete Card Details REST API",
        description = "REST API to delete Card details based on a mobile number"
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
    @DeleteMapping("/card")
    public ResponseEntity<Void> deleteCardDetails(
        @RequestParam
        @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
        String mobileNumber
    ) {
        cardService.deleteCard(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get Build information",
        description = "Get Build information that is deployed into card microservice"
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
    @GetMapping("/build-info")
    public ResponseEntity<String> getBuildInfo() {
        return ResponseEntity
            .ok(buildVersion);
    }

    @Operation(
        summary = "Get Java version",
        description = "Get Java versions details that is installed into card microservice"
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
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(environment.getProperty("JAVA_HOME"));
    }

    @Operation(
        summary = "Get Contact Info",
        description = "Contact Info details that can be reached out in case of any issues"
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
    @GetMapping("/contact-info")
    public ResponseEntity<CardContactInfoDTO> getContactInfo() {

        return ResponseEntity.ok(cardContactInfo);
    }
}