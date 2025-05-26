package io.github.susimsek.account.dto;

public record AccountsMsgDTO(
    Long accountNumber,
    String name,
    String email,
    String mobileNumber) {
}