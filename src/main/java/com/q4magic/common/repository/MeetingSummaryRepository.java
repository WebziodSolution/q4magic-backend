package com.q4magic.common.repository;

import com.q4magic.common.models.MeetingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Integer> {
    @Query(value = "SELECT m FROM MeetingSummary m WHERE m.customers.id = :customerId AND m.opportunities.id = :oppId ORDER BY m.id DESC ")
    List<MeetingSummary> findByOppIdAndCusId(@Param("customerId") Integer customerId, @Param("oppId") Integer oppId);
}