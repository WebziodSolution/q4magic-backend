package com.q4magic.common.repository;

import com.q4magic.common.models.ClosePlan;
import com.q4magic.common.models.SalesProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosePlanRepository extends JpaRepository<ClosePlan, Integer> {
    @Query("SELECT c FROM ClosePlan c WHERE c.opportunities.id=:id")
    List<ClosePlan> findByOppId(Integer id);

    @Query("SELECT c FROM ClosePlan c WHERE c.opportunities.id=:oppId AND c.contacts.id=:contactId AND c.customers.id=:cusId")
    ClosePlan findByOppIdAndContactIdAndCusId(Integer oppId,Integer contactId,Integer cusId);
}
