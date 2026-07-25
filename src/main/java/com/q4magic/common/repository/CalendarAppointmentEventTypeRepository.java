package com.q4magic.common.repository;

import com.q4magic.common.models.CalendarAppointmentEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarAppointmentEventTypeRepository extends JpaRepository<CalendarAppointmentEventType, Integer> {
    @Query("SELECT a FROM CalendarAppointmentEventType a WHERE a.customers.id = :id ORDER BY a.id DESC")
    List<CalendarAppointmentEventType> findByCustomerId(@Param("id") Integer id);
}