package io.github.susimsek.account.filter;

import io.github.susimsek.account.entity.Account;
import io.github.susimsek.account.entity.Account_;
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
public class AccountFilter implements Specification<Account> {

    private String accountType;
    private String branchAddress;
    private Boolean communicationSw;

    @Override
    public Predicate toPredicate(
        Root<Account> root,
        CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {
        var predicates = new LinkedList<Predicate>();

        if (StringUtils.hasText(accountType)) {
            predicates.add(criteriaBuilder.equal(root.get(Account_.ACCOUNT_TYPE), accountType));
        }

        if (StringUtils.hasText(branchAddress)) {
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(
                    root.get(Account_.BRANCH_ADDRESS)), branchAddress.toLowerCase() + "%"));
        }

        if (communicationSw != null) {
            predicates.add(criteriaBuilder.equal(root.get(Account_.COMMUNICATION_SW), communicationSw));
        }

        if (predicates.isEmpty()) {
            return null;
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}