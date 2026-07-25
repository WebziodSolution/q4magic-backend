package com.q4magic.common.repository;

import com.q4magic.common.models.Todo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
    @Query("SELECT t FROM Todo t WHERE t.createdBy.id = :id AND t.isDeleted = false ORDER BY t.id DESC")
    List<Todo> findByCustomerId(@Param("id") Integer id);
}