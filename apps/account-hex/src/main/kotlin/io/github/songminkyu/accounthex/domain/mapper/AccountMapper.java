package io.github.songminkyu.accounthex.domain.mapper;

import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;

/**
 * Mapper for converting between Account domain entities and AccountDTO data transfer objects.
 */
public interface AccountMapper {

    /**
     * Converts an Account entity to an AccountDTO.
     *
     * @param account the account entity
     * @return the account DTO
     */
    AccountDTO toDto(Account account);

    /**
     * Converts an AccountDTO to an Account entity.
     *
     * @param accountDTO the account DTO
     * @return the account entity
     */
    Account toEntity(AccountDTO accountDTO);

    /**
     * Updates an Account entity from an AccountDTO.
     *
     * @param account the account entity to update
     * @param accountDTO the account DTO with updated values
     */
    void updateEntityFromDto(Account account, AccountDTO accountDTO);
}