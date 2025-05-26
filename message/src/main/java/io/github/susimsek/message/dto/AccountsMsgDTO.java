package io.github.susimsek.message.dto;

public record AccountsMsgDTO(
    Long accountNumber,
    String name,
    String email,
    String mobileNumber) {
}