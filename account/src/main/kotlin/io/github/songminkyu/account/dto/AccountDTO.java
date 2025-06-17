package io.github.songminkyu.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
    name = "Account",
    description = "Schema to hold Account information"
)
public record AccountDTO(

    @Schema(
        description = "Account Number of Eazy Bank account", example = "3454433243"
    )
    @NotNull
    Long accountNumber,

    @Schema(
        description = "Account type of Eazy Bank account", example = "Savings"
    )
    @NotBlank
    String accountType,

    @Schema(
        description = "Eazy Bank branch address", example = "123 NewYork"
    )
    @NotBlank
    String branchAddress) {

}