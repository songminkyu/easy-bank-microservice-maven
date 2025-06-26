package io.github.songminkyu.accounthex.domain.model.dto;

/**
 * Data Transfer Object for Account information.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public record AccountDTO(
    Long accountNumber,
    String accountType,
    String branchAddress) {
}
