package com.q4magic.common.repository;

import com.q4magic.common.models.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMembersRepository extends JpaRepository<TeamMembers, Integer> {
    @Query("SELECT t FROM TeamMembers t WHERE t.teamDetails.id = :id")
    List<TeamMembers> findByTeamId(@Param("id") Integer id);

    @Query("SELECT t FROM TeamMembers t WHERE t.customers.id = :id")
    List<TeamMembers> findByMemberId(@Param("id") Integer id);
}