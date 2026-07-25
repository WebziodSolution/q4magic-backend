package com.q4magic.common.repository;

import com.q4magic.common.models.Meetings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface MeetingsRepository extends JpaRepository<Meetings, Integer> {
    @Query(value = "SELECT m FROM Meetings m WHERE m.opportunities.id = :id ORDER BY m.id DESC")
    List<Meetings> findByOppId(@Param("id") Integer id);

    @Query(value = "SELECT m FROM Meetings m WHERE m.calendar.id = :id ORDER BY m.id DESC")
    Meetings findByCalendarId(@Param("id") Integer id);

    @Query(value = "SELECT m FROM Meetings m WHERE m.customers.id = :id ORDER BY m.id DESC")
    List<Meetings> findByCustomerId(@Param("id") Integer id);

    @Query("SELECT COUNT(m) FROM Meetings m WHERE m.customers.id=:customerId")
    int countMeetingByCustomerId(Integer customerId);

    @Query("""
                SELECT m FROM Meetings m
                WHERE m.customers.id = :customerId
                  AND m.createdAt BETWEEN :start AND :end
            """)
    List<Meetings> countMeetingsByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
                SELECT m FROM Meetings m
                WHERE m.customers.id = :customerId
                  AND m.createdAt BETWEEN :start AND :end
            """)
    List<Meetings> findMeetingsByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );
}
