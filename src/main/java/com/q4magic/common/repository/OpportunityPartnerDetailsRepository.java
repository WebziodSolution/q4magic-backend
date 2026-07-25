package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityPartnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityPartnerDetailsRepository extends JpaRepository<OpportunityPartnerDetails, Integer> {
    @Query("SELECT o FROM OpportunityPartnerDetails o WHERE o.opportunities.id = :id AND o.isDeleted = false")
    List<OpportunityPartnerDetails> findPartnersByOppId(@Param("id") Integer id);

    @Query("SELECT o FROM OpportunityPartnerDetails o WHERE o.opportunities.id = :id AND o.accountId = :accountId")
    OpportunityPartnerDetails findPartnersByOppIdAndAccountId(@Param("id") Integer id, @Param("accountId") String accountId);

    @Query("SELECT o FROM OpportunityPartnerDetails o WHERE o.salesforceOpportunityPartnerId = :salesforceOpportunityPartnerId")
    OpportunityPartnerDetails findBySalesforceOpportunityPartnerId(@Param("salesforceOpportunityPartnerId") String salesforceOpportunityPartnerId);
}