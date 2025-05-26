package io.github.susimsek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Card",
    description = "Schema to hold Card information"
)
public record CardDTO(

    @Schema(
        description = "Mobile Number of Customer", example = "4354437687"
    )
    @NotBlank
    @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
    String mobileNumber,


    @Schema(
        description = "Card Number of the customer", example = "100646930341"
    )
    @NotBlank
    @Pattern(regexp = "(^$|\\d{12})", message = "{jakarta.validation.constraint.CardNumber.Pattern.message}")
    String cardNumber,

    @Schema(
        description = "Type of the card", example = "Credit Card"
    )
    @NotBlank
    String cardType,

    @Schema(
        description = "Total amount limit available against a card", example = "100000"
    )
    @Positive
    int totalLimit,

    @Schema(
        description = "Total amount used by a Customer", example = "1000"
    )
    @PositiveOrZero
    int amountUsed,

    @Schema(
        description = "Total available amount against a card", example = "90000"
    )
    @PositiveOrZero
    int availableAmount
) {

}