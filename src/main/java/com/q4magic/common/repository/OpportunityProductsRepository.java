package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityProductsRepository extends JpaRepository<OpportunityProducts, Integer> {
    @Query(value = "SELECT p FROM OpportunityProducts p WHERE p.opportunities.id = :id AND isDeleted = false")
    List<OpportunityProducts> getByOppId(@Param("id") Integer id);

    @Query(value = "SELECT p FROM OpportunityProducts p WHERE p.opportunityProductId = :id")
    OpportunityProducts getBySalesForceOpportunityProductId(@Param("id") String id);

    @Query(value = "SELECT p FROM OpportunityProducts p WHERE p.products.id = :id")
    List<OpportunityProducts> getByProductId(@Param("id") Integer id);
}