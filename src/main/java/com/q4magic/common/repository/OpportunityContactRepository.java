package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityContactRepository extends JpaRepository<OpportunityContact, Integer> {
    @Query("SELECT o FROM OpportunityContact o WHERE o.opportunities.id=:id AND o.isDeleted = false")
    List<OpportunityContact> findByOppId(Integer id);

    @Query("SELECT o FROM OpportunityContact o WHERE o.opportunities.id=:oppId AND o.contacts.id=:contactId")
    List<OpportunityContact> findByOppIdAndContactId(Integer oppId, Integer contactId);

    @Query("SELECT o FROM OpportunityContact o WHERE o.salesforceOpportunityContactId=:id")
    List<OpportunityContact> findBySalesforceOpportunityContactId(String id);

}