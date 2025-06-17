package io.github.songminkyu.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Loan",
    description = "Schema to hold Loan information"
)
public record LoanDTO(

    @Schema(
        description = "Mobile Number of Customer", example = "4365327698"
    )
    String mobileNumber,

    @Schema(
        description = "Loan Number of the customer", example = "548732457654"
    )
    String loanNumber,

    @Schema(
        description = "Type of the loan", example = "Home Loan"
    )
    String loanType,

    @Schema(
        description = "Total loan amount", example = "100000"
    )
    int totalLoan,

    @Schema(
        description = "Total loan amount paid", example = "1000"
    )
    int amountPaid,

    @Schema(
        description = "Total outstanding amount against a loan", example = "99000"
    )
    int outstandingAmount
) {

}