package io.github.songminkyu.accounthex.domain.port.api;

import io.github.songminkyu.accounthex.domain.model.dto.AccountDTO;
import io.github.songminkyu.accounthex.domain.model.dto.CustomerDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Primary port for account operations.
 * This interface defines the operations that can be performed on accounts.
 */
public interface AccountService {

    /**
     * Creates a new account for the given customer.
     *
     * @param customer the customer details
     * @return a CompletableFuture that completes when the account is created
     */
    CompletableFuture<Void> createAccount(CustomerDTO customer);

    /**
     * Fetches account details for the given mobile number.
     *
     * @param mobileNumber the mobile number of the customer
     * @return the customer details including account information
     */
    CustomerDTO fetchAccount(String mobileNumber);

    /**
     * Gets the revision history of an account.
     *
     * @param accountNumber the account number
     * @return a list of account revisions
     */
    List<AccountDTO> getAccountRevisions(Long accountNumber);

    /**
     * Gets the username of the creator of an account.
     *
     * @param accountNumber the account number
     * @return the username of the creator
     */
    String getCreatorUsername(Long accountNumber);

    /**
     * Updates an account with the given details.
     *
     * @param accountNumber the account number
     * @param customer the updated customer details
     * @return true if the account was updated, false otherwise
     */
    boolean updateAccount(Long accountNumber, CustomerDTO customer);

    /**
     * Deletes an account for the given mobile number.
     *
     * @param mobileNumber the mobile number of the customer
     */
    void deleteAccount(String mobileNumber);

    /**
     * Updates the communication status of an account.
     *
     * @param accountNumber the account number
     * @return true if the status was updated, false otherwise
     */
    boolean updateCommunicationStatus(Long accountNumber);
}