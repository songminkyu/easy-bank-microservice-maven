package io.github.songminkyu.loan.mapper;

import io.github.songminkyu.loan.dto.LoanDTO;
import io.github.songminkyu.loan.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanMapper extends EntityMapper<LoanDTO, Loan> {
}
