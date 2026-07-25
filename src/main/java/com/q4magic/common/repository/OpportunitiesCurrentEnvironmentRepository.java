package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunitiesCurrentEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunitiesCurrentEnvironmentRepository extends JpaRepository<OpportunitiesCurrentEnvironment, Integer> {
    @Query("SELECT o FROM OpportunitiesCurrentEnvironment o WHERE o.opportunities.id = :id ORDER BY o.id DESC")
    List<OpportunitiesCurrentEnvironment> findByOppId(@Param("id") Integer id);
}
