package io.github.susimsek.account.repository;

import io.github.susimsek.account.entity.Account;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>,
    RevisionRepository<Account, Long, Integer>,
    JpaSpecificationExecutor<Account> {
    String ACCOUNT_BY_CUSTOMER_ID_CACHE = "accountByCustomerId";

    @Cacheable(cacheNames = ACCOUNT_BY_CUSTOMER_ID_CACHE)
    Optional<Account> findByCustomerId(Long customerId);

    @Transactional
    @Modifying
    void deleteByCustomerId(Long customerId);

}