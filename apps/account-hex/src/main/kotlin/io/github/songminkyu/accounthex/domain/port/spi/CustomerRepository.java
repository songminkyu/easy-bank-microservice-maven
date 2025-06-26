package io.github.songminkyu.accounthex.domain.port.spi;

import io.github.songminkyu.accounthex.domain.model.Customer;

import java.util.Optional;

/**
 * Secondary port for customer persistence operations.
 * This interface defines the operations that can be performed on customer data.
 */
public interface CustomerRepository {

    /**
     * Saves a customer.
     *
     * @param customer the customer to save
     * @return the saved customer
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by its ID.
     *
     * @param customerId the customer ID
     * @return an Optional containing the customer if found, or empty if not found
     */
    Optional<Customer> findById(Long customerId);

    /**
     * Finds a customer by its mobile number.
     *
     * @param mobileNumber the mobile number
     * @return an Optional containing the customer if found, or empty if not found
     */
    Optional<Customer> findByMobileNumber(String mobileNumber);

    /**
     * Checks if a customer exists with the given mobile number.
     *
     * @param mobileNumber the mobile number
     * @return true if a customer exists with the given mobile number, false otherwise
     */
    boolean existsByMobileNumber(String mobileNumber);

    /**
     * Deletes a customer by its ID.
     *
     * @param customerId the customer ID
     */
    void deleteById(Long customerId);
}