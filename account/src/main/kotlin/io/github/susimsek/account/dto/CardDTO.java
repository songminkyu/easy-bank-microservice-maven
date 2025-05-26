package io.github.susimsek.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(name = "Card",
    description = "Schema to hold Card information"
)
public record CardDTO(

    @Schema(
        description = "Mobile Number of Customer", example = "4354437687"
    )
    String mobileNumber,


    @Schema(
        description = "Card Number of the customer", example = "100646930341"
    )
    @NotEmpty(message = "Card Number can not be a null or empty")
    String cardNumber,

    @Schema(
        description = "Type of the card", example = "Credit Card"
    )
    String cardType,

    @Schema(
        description = "Total amount limit available against a card", example = "100000"
    )
    int totalLimit,

    @Schema(
        description = "Total amount used by a Customer", example = "1000"
    )
    int amountUsed,

    @Schema(
        description = "Total available amount against a card", example = "90000"
    )
    int availableAmount
) {

}