package io.github.susimsek.account.mapper;

import io.github.susimsek.account.dto.CardDTO;
import io.github.susimsek.account.dto.CustomerDTO;
import io.github.susimsek.account.dto.CustomerDetailsDTO;
import io.github.susimsek.account.dto.LoanDTO;
import io.github.susimsek.account.entity.Account;
import io.github.susimsek.account.entity.Customer;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(
    componentModel = "spring",
    uses = AccountMapper.class,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {

    CustomerDTO toDto(Customer customer, Account account);

    @Mapping(source = "customer.mobileNumber", target = "mobileNumber")
    CustomerDetailsDTO toCustomerDetailsDTO(
        Customer customer,
        Account account,
        LoanDTO loan,
        CardDTO card);
}
