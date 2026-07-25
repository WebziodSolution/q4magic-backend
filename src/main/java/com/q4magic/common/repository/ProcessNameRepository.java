package com.q4magic.common.repository;

import com.q4magic.common.models.ProcessName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessNameRepository extends JpaRepository<ProcessName, Integer> {
    @Query("SELECT p FROM ProcessName p WHERE p.opportunities.id=:oppId")
    List<ProcessName> findByOppId(Integer oppId);

    @Query("SELECT p FROM ProcessName p WHERE p.customers.id=:cusId")
    List<ProcessName> findByContactId(Integer cusId);

    @Query("SELECT p FROM ProcessName p WHERE  p.opportunities.id=:oppId AND p.name=:name")
    ProcessName findByOppIdAndName(Integer oppId,String name);
}
