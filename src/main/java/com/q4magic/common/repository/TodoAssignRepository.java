package com.q4magic.common.repository;

import com.q4magic.common.models.TodoAssign;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoAssignRepository extends JpaRepository<TodoAssign, Integer>, JpaSpecificationExecutor<TodoAssign> {
    @Query(value = "SELECT t FROM TodoAssign t WHERE t.assignBy.id = :id")
    List<TodoAssign> getAllAssignBy(@Param("id") Integer id);

    @Query(value = "SELECT t FROM TodoAssign t WHERE t.todo.id = :id")
    List<TodoAssign> getByTodoId(@Param("id") Integer id);

    @Query(value = "SELECT t FROM TodoAssign t WHERE t.customers.id = :id AND t.teamDetails.id = :teamId AND t.todo.id = :todoId")
    TodoAssign getByCustomerTeamAndTodoId(@Param("id") Integer id, @Param("teamId") Integer teamId, @Param("todoId") Integer todoId);

    @Query(value = "SELECT t FROM TodoAssign t WHERE t.customers.id = :id AND t.todo.id = :todoId")
    TodoAssign getByCustomerAndTodo(@Param("id") Integer id, @Param("todoId") Integer todoId);

    @Query("SELECT DISTINCT t.todo.id " +
            "FROM TodoAssign t " +
            "WHERE t.assignBy.id = :id " +
            "AND (:teamIds IS NULL OR t.teamDetails.id IN :teamIds) " +
            "AND t.complectedWork BETWEEN :minWork AND :maxWork")
    List<Integer> findTodoIdsByStatus(
            @Param("id") Integer id,
            @Param("teamIds") List<Integer> teamIds,
            @Param("minWork") Integer minWork,
            @Param("maxWork") Integer maxWork
    );
}