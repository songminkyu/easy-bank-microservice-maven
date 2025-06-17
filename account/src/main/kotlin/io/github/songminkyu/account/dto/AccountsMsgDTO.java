package io.github.songminkyu.account.dto;

public record AccountsMsgDTO(
    Long accountNumber,
    String name,
    String email,
    String mobileNumber) {
}