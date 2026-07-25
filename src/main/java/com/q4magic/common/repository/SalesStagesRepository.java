package com.q4magic.common.repository;

import com.q4magic.common.models.SalesStages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesStagesRepository extends JpaRepository<SalesStages, Integer> {
    @Query("SELECT s FROM SalesStages s WHERE s.salesforceStageId=:salesforceStageId")
    SalesStages findBySalesforceStageId(String salesforceStageId);
}
