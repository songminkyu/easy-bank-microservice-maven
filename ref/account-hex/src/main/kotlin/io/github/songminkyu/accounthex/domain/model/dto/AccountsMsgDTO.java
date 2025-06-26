package io.github.songminkyu.accounthex.domain.model.dto;

import java.util.Objects;

/**
 * Data Transfer Object for account messages.
 * This class is used for sending account-related messages to external systems.
 * This is a pure domain DTO without any framework-specific annotations.
 */
public class AccountsMsgDTO {

    private Long accountNumber;
    private String name;
    private String email;
    private String mobileNumber;

    public AccountsMsgDTO() {
    }

    public AccountsMsgDTO(Long accountNumber, String name, String email, String mobileNumber) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    // Getters
    public Long getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    // Setters
    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountsMsgDTO that = (AccountsMsgDTO) o;
        return Objects.equals(accountNumber, that.accountNumber) &&
               Objects.equals(name, that.name) &&
               Objects.equals(email, that.email) &&
               Objects.equals(mobileNumber, that.mobileNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber, name, email, mobileNumber);
    }

    @Override
    public String toString() {
        return "AccountsMsgDTO{" +
                "accountNumber=" + accountNumber +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}
