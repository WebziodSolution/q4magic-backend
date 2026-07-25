package com.q4magic.common.repository;

import com.q4magic.common.models.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Integer> {
    @Query("SELECT a FROM Performance a WHERE a.customers.id = :id ORDER BY a.id DESC")
    Performance findByCustomerId(@Param("id") Integer id);

}