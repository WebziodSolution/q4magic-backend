package com.q4magic.common.specification;

import com.q4magic.common.models.Opportunities;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class    OpportunitiesSpecification {

    public static Specification<Opportunities> notDeleted() {
        return (root, q, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Opportunities> search(String search) {
        // Return null or an 'always true' specification if the search string is empty
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        // Create a lowercase pattern for case-insensitive partial matching
        String pattern = "%" + search.toLowerCase() + "%";

        return (root, query, cb) -> {
            // Build the "LIKE" predicates for both fields
            var opportunityLike = cb.like(cb.lower(root.get("opportunity")), pattern);
            var accountLike = cb.like(cb.lower(root.get("account").get("companyName")), pattern);
            var accountLike1 = cb.like(cb.lower(root.get("account").get("accountName")), pattern);

            // Return a combined predicate using OR logic
            return cb.or(opportunityLike, accountLike ,accountLike1);
        };
    }

    public static Specification<Opportunities> salesStageIn(List<String> stages) {
        if (stages == null || stages.isEmpty()) return null;
        List<String> lowered = stages.stream().filter(Objects::nonNull)
                .map(String::toLowerCase).toList();
        return (root, q, cb) -> cb.lower(root.get("salesStage")).in(lowered);
    }

    public static Specification<Opportunities> statusIn(List<String> status) {
        if (status == null || status.isEmpty()) return null;
        List<String> lowered = status.stream().filter(Objects::nonNull)
                .map(String::toLowerCase).toList();
        return (root, q, cb) -> cb.lower(root.get("status")).in(lowered);
    }
    public static Specification<Opportunities> customerIdIn(Collection<Integer> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) return null;
        return (root, q, cb) -> root.get("customers").get("id").in(customerIds);
    }
}