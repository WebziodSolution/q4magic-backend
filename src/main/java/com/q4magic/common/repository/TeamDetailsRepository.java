package com.q4magic.common.repository;

import com.q4magic.common.models.TeamDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamDetailsRepository extends JpaRepository<TeamDetails, Integer> {
    @Query("SELECT t FROM TeamDetails t WHERE t.customers.id = :id")
    List<TeamDetails> findCreatedBy(@Param("id") Integer id);

    @Query("SELECT t FROM TeamDetails t WHERE t.assignMember.id = :id")
    List<TeamDetails> getAllAssignTeams(@Param("id") Integer id);
}