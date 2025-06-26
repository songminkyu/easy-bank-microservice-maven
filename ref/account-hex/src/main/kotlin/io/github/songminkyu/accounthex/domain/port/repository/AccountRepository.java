package io.github.songminkyu.accounthex.domain.port.repository;

import io.github.songminkyu.accounthex.domain.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Secondary port for account persistence operations.
 * This interface defines the operations that can be performed on account data.
 */
public interface AccountRepository {

    /**
     * Saves an account.
     *
     * @param account the account to save
     * @return the saved account
     */
    Account save(Account account);

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber the account number
     * @return an Optional containing the account if found, or empty if not found
     */
    Optional<Account> findById(Long accountNumber);

    /**
     * Finds an account by its customer ID.
     *
     * @param customerId the customer ID
     * @return an Optional containing the account if found, or empty if not found
     */
    Optional<Account> findByCustomerId(Long customerId);

    /**
     * Deletes an account by its customer ID.
     *
     * @param customerId the customer ID
     */
    void deleteByCustomerId(Long customerId);

    /**
     * Finds all revisions of an account.
     *
     * @param accountNumber the account number
     * @return a list of account revisions
     */
    List<Revision<Account>> findRevisions(Long accountNumber);

    /**
     * Finds a specific revision of an account.
     *
     * @param accountNumber the account number
     * @param revisionNumber the revision number
     * @return an Optional containing the revision if found, or empty if not found
     */
    Optional<Revision<Account>> findRevision(Long accountNumber, int revisionNumber);

    /**
     * A simple interface to represent a revision of an entity.
     *
     * @param <T> the entity type
     */
    interface Revision<T> {
        /**
         * Gets the entity at this revision.
         *
         * @return the entity
         */
        T getEntity();

        /**
         * Gets the metadata for this revision.
         *
         * @return the revision metadata
         */
        RevisionMetadata getMetadata();
    }

    /**
     * A simple interface to represent revision metadata.
     */
    interface RevisionMetadata {
        /**
         * Gets the delegate object that contains the actual metadata.
         *
         * @return the delegate object
         */
        Object getDelegate();
    }
}