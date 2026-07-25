package com.q4magic.common.repository;

import com.q4magic.common.models.SalesProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesProcessRepository extends JpaRepository<SalesProcess, Integer> {
    @Query("SELECT s FROM SalesProcess s WHERE s.opportunities.id=:id")
    List<SalesProcess> findByOppId(Integer id);
}