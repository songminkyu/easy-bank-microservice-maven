package io.github.susimsek.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Loan",
    description = "Schema to hold Loan information"
)
public record LoanDTO(

    @Schema(
        description = "Mobile Number of Customer", example = "4365327698"
    )
    @NotBlank
    @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
    String mobileNumber,

    @Schema(
        description = "Loan Number of the customer", example = "548732457654"
    )
    @NotBlank
    @Pattern(regexp = "(^$|\\d{12})", message = "{jakarta.validation.constraint.LoanNumber.Pattern.message}")
    String loanNumber,

    @Schema(
        description = "Type of the loan", example = "Home Loan"
    )
    @NotBlank
    String loanType,

    @Schema(
        description = "Total loan amount", example = "100000"
    )
    @Positive
    int totalLoan,

    @Schema(
        description = "Total loan amount paid", example = "1000"
    )
    @PositiveOrZero
    int amountPaid,

    @PositiveOrZero
    @Schema(
        description = "Total outstanding amount against a loan", example = "99000"
    )
    int outstandingAmount
) {

}