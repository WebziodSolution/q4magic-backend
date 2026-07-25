package com.q4magic.common.specification;

import com.q4magic.common.models.TodoAssign;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class TodoSpecification {

    public static Specification<TodoAssign> teamIdIn(List<Integer> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) return null;
        return (root, q, cb) -> root.get("teamDetails").get("id").in(teamIds);
    }

    public static Specification<TodoAssign> hasStatus(String status) {
        if (status == null || "All".equalsIgnoreCase(status)) return null;

        return (root, q, cb) -> {
            if ("Completed".equalsIgnoreCase(status)) {
                return cb.equal(root.get("todo").get("complectedWork"), 100);
            } else if ("Pending".equalsIgnoreCase(status)) {
                // Pending is usually < 100
                return cb.lessThan(root.get("todo").get("complectedWork"), 100);
            }
            return null;
        };
    }

    public static Specification<TodoAssign> assignedToMe(Integer customerId) {
        if (customerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("customers").get("id"), customerId);
    }

    public static Specification<TodoAssign> createdByMe(Integer customerId) {
        if (customerId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("assignBy").get("id"), customerId);
    }
}