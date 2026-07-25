package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityLineItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityLineItemsRepository extends JpaRepository<OpportunityLineItems, Integer> {
    @Query("SELECT o FROM OpportunityLineItems o WHERE o.opportunities.id=:id")
    List<OpportunityLineItems> findByOppId(Integer id);
}