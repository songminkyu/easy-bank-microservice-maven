package io.github.songminkyu.accounthex.domain.model.dto;

/**
 * Data Transfer Object for Customer information.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public record CustomerDTO(
    String name,
    String email,
    String mobileNumber,
    AccountDTO account) {
}
