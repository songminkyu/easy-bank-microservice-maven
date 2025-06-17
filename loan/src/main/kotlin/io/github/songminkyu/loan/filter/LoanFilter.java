package io.github.songminkyu.loan.filter;

import io.github.songminkyu.loan.entity.Loan;
import io.github.songminkyu.loan.entity.Loan_;
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
public class LoanFilter implements Specification<Loan> {

    private String loanType;
    private Number totalLoan;

    @Override
    public Predicate toPredicate(
        Root<Loan> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {
        var predicates = new LinkedList<Predicate>();

        if (StringUtils.hasText(loanType)) {
            predicates.add(criteriaBuilder.equal(root.get(Loan_.LOAN_TYPE), loanType));
        }

        if (totalLoan != null) {
            predicates.add(criteriaBuilder.ge(root.get(Loan_.TOTAL_LOAN), totalLoan));
        }

        if (predicates.isEmpty()) {
            return null;
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}