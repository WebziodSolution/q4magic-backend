package com.q4magic.common.repository;

import com.q4magic.common.models.TodoAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoAttachmentsRepository extends JpaRepository<TodoAttachments, Integer> {

    @Query(value = "SELECT t FROM TodoAttachments t WHERE t.todo.id = :id")
    List<TodoAttachments> getByTodoId(@Param("id") Integer id);
}