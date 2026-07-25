package com.q4magic.common.repository;

import com.q4magic.common.models.OpportunityContactNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityContactNotesRepository extends JpaRepository<OpportunityContactNotes, Integer> {
    @Query("SELECT o FROM OpportunityContactNotes o " +
            "WHERE o.opportunitiesContact.id = :contactId " +
            "ORDER BY CASE WHEN o.type = 'professional' THEN 1 ELSE 2 END ASC, o.id DESC")
    List<OpportunityContactNotes> findByContactId(@Param("contactId") Integer contactId);
}