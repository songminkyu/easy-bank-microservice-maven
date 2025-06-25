package io.github.songminkyu.account.repository;

import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;

import io.github.songminkyu.account.entity.Customer;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    boolean existsByMobileNumber(String mobileNumber);

    Optional<Customer> findByMobileNumber(String mobileNumber);
}