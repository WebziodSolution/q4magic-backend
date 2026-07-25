package com.q4magic.common.repository;

import com.q4magic.common.models.ITLandscape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITLandscapeRepository extends JpaRepository<ITLandscape, Integer> {
    @Query("SELECT i FROM ITLandscape i WHERE i.salesforceOpportunityId=:salesforceOpportunityId")
    List<ITLandscape> findBySalesforceOpportunityId(String salesforceOpportunityId);

    @Query("SELECT i FROM ITLandscape i WHERE i.salesforceCompetitorId=:salesforceCompetitorId")
    ITLandscape findBySalesforceCompetitorId(String salesforceCompetitorId);
}
