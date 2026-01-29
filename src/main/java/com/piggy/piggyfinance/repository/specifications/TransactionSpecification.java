package com.piggy.piggyfinance.repository.specifications;

import com.piggy.piggyfinance.model.Transaction;
import com.piggy.piggyfinance.model.filters.TransactionFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> byFilter(TransactionFilter filter, String userEmail) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // sempre filtrar pelo usuário
            predicates.add(cb.equal(root.get("user").get("email"), userEmail));

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("timestamp"),
                        filter.getStartDate().atStartOfDay()
                ));
            }

            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("timestamp"),
                        filter.getEndDate().atTime(LocalTime.MAX)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
