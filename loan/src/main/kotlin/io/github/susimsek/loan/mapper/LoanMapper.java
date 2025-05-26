package io.github.susimsek.loan.mapper;

import io.github.susimsek.loan.dto.LoanDTO;
import io.github.susimsek.loan.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanMapper extends EntityMapper<LoanDTO, Loan> {
}
