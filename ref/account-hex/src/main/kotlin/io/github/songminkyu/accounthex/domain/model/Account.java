package io.github.songminkyu.accounthex.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Account domain entity.
 * This is a pure domain entity without any framework-specific annotations.
 */
public class Account {

    private Long accountNumber;
    private Long customerId;
    private String accountType;
    private String branchAddress;
    private Boolean communicationSw;
    private Boolean deleted;

    // Audit fields
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public Account() {
    }

    public Account(Long accountNumber, Long customerId, String accountType, String branchAddress, 
                  Boolean communicationSw, Boolean deleted, Instant createdAt, String createdBy, 
                  Instant updatedAt, String updatedBy) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.branchAddress = branchAddress;
        this.communicationSw = communicationSw;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Getters
    public Long getAccountNumber() {
        return accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public Boolean getCommunicationSw() {
        return communicationSw;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters
    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public void setCommunicationSw(Boolean communicationSw) {
        this.communicationSw = communicationSw;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Builder pattern implementation
    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private Long accountNumber;
        private Long customerId;
        private String accountType;
        private String branchAddress;
        private Boolean communicationSw;
        private Boolean deleted;
        private Instant createdAt;
        private String createdBy;
        private Instant updatedAt;
        private String updatedBy;

        public AccountBuilder accountNumber(Long accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountBuilder accountType(String accountType) {
            this.accountType = accountType;
            return this;
        }

        public AccountBuilder branchAddress(String branchAddress) {
            this.branchAddress = branchAddress;
            return this;
        }

        public AccountBuilder communicationSw(Boolean communicationSw) {
            this.communicationSw = communicationSw;
            return this;
        }

        public AccountBuilder deleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public AccountBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccountBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public AccountBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AccountBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Account build() {
            return new Account(accountNumber, customerId, accountType, branchAddress, 
                              communicationSw, deleted, createdAt, createdBy, updatedAt, updatedBy);
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber=" + accountNumber +
                ", customerId=" + customerId +
                ", accountType='" + accountType + '\'' +
                ", branchAddress='" + branchAddress + '\'' +
                ", communicationSw=" + communicationSw +
                ", deleted=" + deleted +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Account account = (Account) obj;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}
