package io.github.songminkyu.accounthex.domain.model.dto;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for detailed customer information including related services.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public class CustomerDetailsDTO {

    private String name;
    private String email;
    private String mobileNumber;
    private AccountDTO account;
    private List<CardDTO> cards;
    private List<LoanDTO> loans;

    public CustomerDetailsDTO() {
    }

    public CustomerDetailsDTO(String name, String email, String mobileNumber, 
                             AccountDTO account, List<CardDTO> cards, List<LoanDTO> loans) {
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.account = account;
        this.cards = cards;
        this.loans = loans;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public AccountDTO getAccount() {
        return account;
    }

    public List<CardDTO> getCards() {
        return cards;
    }

    public List<LoanDTO> getLoans() {
        return loans;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setAccount(AccountDTO account) {
        this.account = account;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }

    public void setLoans(List<LoanDTO> loans) {
        this.loans = loans;
    }

    // Builder pattern implementation
    public static CustomerDetailsBuilder builder() {
        return new CustomerDetailsBuilder();
    }

    public static class CustomerDetailsBuilder {
        private String name;
        private String email;
        private String mobileNumber;
        private AccountDTO account;
        private List<CardDTO> cards;
        private List<LoanDTO> loans;

        public CustomerDetailsBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CustomerDetailsBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CustomerDetailsBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public CustomerDetailsBuilder account(AccountDTO account) {
            this.account = account;
            return this;
        }

        public CustomerDetailsBuilder cards(List<CardDTO> cards) {
            this.cards = cards;
            return this;
        }

        public CustomerDetailsBuilder loans(List<LoanDTO> loans) {
            this.loans = loans;
            return this;
        }

        public CustomerDetailsDTO build() {
            return new CustomerDetailsDTO(name, email, mobileNumber, account, cards, loans);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDetailsDTO that = (CustomerDetailsDTO) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(email, that.email) &&
               Objects.equals(mobileNumber, that.mobileNumber) &&
               Objects.equals(account, that.account) &&
               Objects.equals(cards, that.cards) &&
               Objects.equals(loans, that.loans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, mobileNumber, account, cards, loans);
    }

    @Override
    public String toString() {
        return "CustomerDetailsDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", account=" + account +
                ", cards=" + cards +
                ", loans=" + loans +
                '}';
    }
}
