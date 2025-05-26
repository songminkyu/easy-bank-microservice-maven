package io.github.susimsek.card.repository;

import static org.hibernate.jpa.HibernateHints.HINT_CACHEABLE;

import io.github.susimsek.card.entity.Card;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>,
    JpaSpecificationExecutor<Card> {

    String CARD_BY_MOBILE_NUMBER_CACHE = "cardByMobileNumber";

    @Cacheable(cacheNames = CARD_BY_MOBILE_NUMBER_CACHE)
    Optional<Card> findByMobileNumber(String mobileNumber);

    @QueryHints(@QueryHint(name = HINT_CACHEABLE, value = "true"))
    boolean existsByMobileNumber(String mobileNumber);

    Optional<Card> findByCardNumber(String cardNumber);

    Window<Card> findAllBy(ScrollPosition position, Limit limit, Sort sort);

}