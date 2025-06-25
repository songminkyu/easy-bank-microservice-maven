package io.github.songminkyu.loan.service.impl;

import static io.github.songminkyu.loan.constants.Constants.RANDOM;

import io.github.songminkyu.loan.constants.LoanConstants;
import io.github.songminkyu.loan.dto.LoanDTO;
import io.github.songminkyu.loan.entity.Loan;
import io.github.songminkyu.loan.exception.EntityNotFoundException;
import io.github.songminkyu.loan.exception.LoanAlreadyExistsException;
import io.github.songminkyu.loan.mapper.LoanMapper;
import io.github.songminkyu.loan.repository.LoanRepository;
import io.github.songminkyu.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    @Override
    public void createLoan(String mobileNumber) {
        if (loanRepository.existsByMobileNumber(mobileNumber)) {
            throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber " + mobileNumber);
        }
        loanRepository.save(createNewLoan(mobileNumber));
    }

    @Override
    public LoanDTO fetchLoan(String mobileNumber) {
        var loan = loanRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Loan.class, "mobileNumber", mobileNumber)
        );
        return loanMapper.toDto(loan);
    }

    @Override
    public boolean updateLoan(String loanNumber, LoanDTO loan) {
        var loanEntity = loanRepository.findByLoanNumber(loanNumber).orElseThrow(
            () -> new EntityNotFoundException(Loan.class, "loanNumber", loanNumber)
        );
        loanMapper.partialUpdate(loanEntity, loan);
        loanEntity.setLoanNumber(loanNumber);
        loanRepository.save(loanEntity);
        return true;
    }

    @Override
    public void deleteLoan(String mobileNumber) {
        var loan = loanRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Loan.class, "mobileNumber", mobileNumber)
        );
        loanRepository.deleteById(loan.getLoanId());
    }

    private Loan createNewLoan(String mobileNumber) {
        Loan newLoan = new Loan();
        long randomLoanNumber = 100000000000L + RANDOM.nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoanConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoanConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoanConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }
}
