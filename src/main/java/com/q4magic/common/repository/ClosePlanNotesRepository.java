package com.q4magic.common.repository;

import com.q4magic.common.models.ClosePlanNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Repository
public interface ClosePlanNotesRepository extends JpaRepository<ClosePlanNotes, Integer> {
    @Query("""
                SELECT n
                FROM ClosePlanNotes n
                WHERE n.closePlan.id = :closePlanId
                  AND (
                        n.sendTo = :contactId
                        OR n.createdBy  = :contactId
                     OR n.sendTo IS NOT NULL
                     OR n.createdBy IS NOT NULL
                  )
                ORDER BY n.createdAt DESC, n.id DESC
            """)
    List<ClosePlanNotes> findByClosePlanIdAndContactIds(
            @Param("closePlanId") Integer closePlanId,
            @Param("contactId") Integer contactId
    );

    @Query("""
                SELECT n FROM ClosePlanNotes n
                WHERE n.closePlan.id = :closePlanId
                ORDER BY n.createdAt DESC, n.id DESC
            """)
    List<ClosePlanNotes> findLastTwoComments(
            @Param("closePlanId") Integer closePlanId,
            Pageable pageable
    );

}
