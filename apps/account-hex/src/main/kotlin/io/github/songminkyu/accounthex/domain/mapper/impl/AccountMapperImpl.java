package io.github.songminkyu.accounthex.domain.mapper.impl;

import io.github.songminkyu.accounthex.domain.mapper.AccountMapper;
import io.github.songminkyu.accounthex.domain.model.Account;
import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;

/**
 * Implementation of the AccountMapper interface.
 * This class provides methods for converting between Account domain entities and AccountDTO data transfer objects.
 * This is a pure domain implementation without any framework-specific annotations.
 */
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountDTO toDto(Account account) {
        if (account == null) {
            return null;
        }

        return new AccountDTO(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBranchAddress()
        );
    }

    @Override
    public Account toEntity(AccountDTO accountDTO) {
        if (accountDTO == null) {
            return null;
        }

        Account account = new Account();
        account.setAccountNumber(accountDTO.accountNumber());
        account.setAccountType(accountDTO.accountType());
        account.setBranchAddress(accountDTO.branchAddress());
        account.setDeleted(false);
        return account;
    }

    @Override
    public void updateEntityFromDto(Account account, AccountDTO accountDTO) {
        if (accountDTO == null) {
            return;
        }

        if (accountDTO.accountType() != null) {
            account.setAccountType(accountDTO.accountType());
        }
        if (accountDTO.branchAddress() != null) {
            account.setBranchAddress(accountDTO.branchAddress());
        }
    }
}
