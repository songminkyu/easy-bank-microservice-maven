package io.github.songminkyu.loan.service;

import io.github.songminkyu.loan.dto.LoanDTO;

public interface LoanService {

    void createLoan(String mobileNumber);

    LoanDTO fetchLoan(String mobileNumber);

    boolean updateLoan(String loanNumber, LoanDTO loan);

    void deleteLoan(String mobileNumber);
}
