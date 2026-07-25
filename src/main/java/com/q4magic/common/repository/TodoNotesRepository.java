package com.q4magic.common.repository;

import com.q4magic.common.models.TodoNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoNotesRepository extends JpaRepository<TodoNotes, Integer> {

    @Query(value = "SELECT t FROM TodoNotes t WHERE t.todo.id = :id")
    List<TodoNotes> getByTodoId(@Param("id") Integer id);

    @Query(value = "SELECT t FROM TodoNotes t WHERE t.customers.id = :id")
    List<TodoNotes> getByCustomer(@Param("id") Integer id);
}