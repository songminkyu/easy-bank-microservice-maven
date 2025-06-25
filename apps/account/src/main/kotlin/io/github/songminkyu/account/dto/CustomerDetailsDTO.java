package io.github.songminkyu.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "CustomerDetails",
    description = "Schema to hold Customer, Account, Card and Loan information"
)
public record CustomerDetailsDTO(
    @Schema(description = "Name of the customer", example = "Eazy Bytes") String name,
    @Schema(description = "Email address of the customer", example = "tutor@eazybytes.com")
    String email,
    @Schema(description = "Mobile Number of the customer", example = "9345432123")
    String mobileNumber,

    @Schema(description = "Account details of the Customer")
    AccountDTO account,
    @Schema(description = "Loan details of the Customer")
    LoanDTO loan,

    @Schema(description = "Card details of the Customer")
    CardDTO card) {

}