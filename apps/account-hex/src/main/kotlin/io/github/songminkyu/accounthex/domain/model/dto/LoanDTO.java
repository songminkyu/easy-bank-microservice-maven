package io.github.songminkyu.accounthex.domain.model.dto;

import java.util.Objects;

/**
 * Data Transfer Object for Loan information.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public class LoanDTO {

    private String mobileNumber;
    private String loanNumber;
    private String loanType;
    private int totalLoan;
    private int amountPaid;
    private int outstandingAmount;

    public LoanDTO() {
    }

    public LoanDTO(String mobileNumber, String loanNumber, String loanType, 
                  int totalLoan, int amountPaid, int outstandingAmount) {
        this.mobileNumber = mobileNumber;
        this.loanNumber = loanNumber;
        this.loanType = loanType;
        this.totalLoan = totalLoan;
        this.amountPaid = amountPaid;
        this.outstandingAmount = outstandingAmount;
    }

    // Getters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public String getLoanType() {
        return loanType;
    }

    public int getTotalLoan() {
        return totalLoan;
    }

    public int getAmountPaid() {
        return amountPaid;
    }

    public int getOutstandingAmount() {
        return outstandingAmount;
    }

    // Setters
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public void setTotalLoan(int totalLoan) {
        this.totalLoan = totalLoan;
    }

    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setOutstandingAmount(int outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    // Builder pattern implementation
    public static LoanDTOBuilder builder() {
        return new LoanDTOBuilder();
    }

    public static class LoanDTOBuilder {
        private String mobileNumber;
        private String loanNumber;
        private String loanType;
        private int totalLoan;
        private int amountPaid;
        private int outstandingAmount;

        public LoanDTOBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public LoanDTOBuilder loanNumber(String loanNumber) {
            this.loanNumber = loanNumber;
            return this;
        }

        public LoanDTOBuilder loanType(String loanType) {
            this.loanType = loanType;
            return this;
        }

        public LoanDTOBuilder totalLoan(int totalLoan) {
            this.totalLoan = totalLoan;
            return this;
        }

        public LoanDTOBuilder amountPaid(int amountPaid) {
            this.amountPaid = amountPaid;
            return this;
        }

        public LoanDTOBuilder outstandingAmount(int outstandingAmount) {
            this.outstandingAmount = outstandingAmount;
            return this;
        }

        public LoanDTO build() {
            return new LoanDTO(mobileNumber, loanNumber, loanType, totalLoan, amountPaid, outstandingAmount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanDTO loanDTO = (LoanDTO) o;
        return totalLoan == loanDTO.totalLoan &&
               amountPaid == loanDTO.amountPaid &&
               outstandingAmount == loanDTO.outstandingAmount &&
               Objects.equals(mobileNumber, loanDTO.mobileNumber) &&
               Objects.equals(loanNumber, loanDTO.loanNumber) &&
               Objects.equals(loanType, loanDTO.loanType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mobileNumber, loanNumber, loanType, totalLoan, amountPaid, outstandingAmount);
    }

    @Override
    public String toString() {
        return "LoanDTO{" +
                "mobileNumber='" + mobileNumber + '\'' +
                ", loanNumber='" + loanNumber + '\'' +
                ", loanType='" + loanType + '\'' +
                ", totalLoan=" + totalLoan +
                ", amountPaid=" + amountPaid +
                ", outstandingAmount=" + outstandingAmount +
                '}';
    }
}
