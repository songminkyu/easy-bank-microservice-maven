package io.github.susimsek.account.mapper;

import io.github.susimsek.account.dto.AccountDTO;
import io.github.susimsek.account.entity.Account;
import io.github.susimsek.account.mapper.util.AccountMappingUtil;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = AccountMappingUtil.class)
public interface AccountMapper extends EntityMapper<AccountDTO, Account> {


    @Mapping(source = ".", target = "accountNumber", qualifiedBy = AccountMappingUtil.AccountNumber.class)
    @Mapping(source = ".", target = "accountType", qualifiedBy = AccountMappingUtil.AccountType.class)
    @Mapping(source = ".", target = "branchAddress", qualifiedByName = "branchAddress")
    AccountDTO fromMap(Map<String, Object> map);
}
