package io.github.susimsek.card.filter;

import io.github.susimsek.card.entity.Card;
import io.github.susimsek.card.entity.Card_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.LinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardFilter implements Specification<Card> {

    private String cardType;
    private Number totalLimit;

    @Override
    public Predicate toPredicate(
        Root<Card> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {
        var predicates = new LinkedList<Predicate>();

        if (StringUtils.hasText(cardType)) {
            predicates.add(criteriaBuilder.equal(root.get(Card_.CARD_TYPE), cardType));
        }

        if (totalLimit != null) {
            predicates.add(criteriaBuilder.ge(root.get(Card_.TOTAL_LIMIT), totalLimit));
        }

        if (predicates.isEmpty()) {
            return null;
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}