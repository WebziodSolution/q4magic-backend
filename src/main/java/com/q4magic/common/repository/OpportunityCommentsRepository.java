package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityCommentsRepository extends JpaRepository<OpportunityComments, Integer>, JpaSpecificationExecutor<OpportunityComments> {
    @Query("SELECT o FROM OpportunityComments o WHERE o.customers.id = :cusId ANd o.opportunities.id = :oppId ORDER BY o.id DESC")
    List<OpportunityComments> findByOppIdAndCustomerId(@Param("cusId") Integer cusId, @Param("oppId") Integer oppId);
}
