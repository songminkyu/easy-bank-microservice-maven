package io.github.songminkyu.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(
    name = "Customer",
    description = "Schema to hold Customer and Account information"
)
public record CustomerDTO(

    @Schema(
        description = "Name of the customer", example = "Eazy Bytes"
    )
    @NotBlank
    @Size(min = 5, max = 30)
    String name,

    @Schema(
        description = "Email address of the customer", example = "tutor@eazybytes.com"
    )
    @NotBlank
    @Email
    String email,

    @Schema(
        description = "Mobile Number of the customer", example = "9345432123"
    )
    @Pattern(regexp = "(^$|\\d{10})", message = "{jakarta.validation.constraint.MobileNumber.Pattern.message}")
    String mobileNumber,

    @Schema(
        description = "Account details of the Customer"
    )
    @Valid
    AccountDTO account) {

}