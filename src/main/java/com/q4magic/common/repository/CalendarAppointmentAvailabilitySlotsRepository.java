package com.q4magic.common.repository;

import com.q4magic.common.models.CalendarAppointmentAvailabilitySlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarAppointmentAvailabilitySlotsRepository extends JpaRepository<CalendarAppointmentAvailabilitySlots, Integer> {
    @Query("SELECT c FROM CalendarAppointmentAvailabilitySlots c WHERE c.customers.id = :id ORDER BY c.id DESC")
    List<CalendarAppointmentAvailabilitySlots> findByCustomerId(@Param("id") Integer id);

    @Query("""
SELECT c
FROM CalendarAppointmentAvailabilitySlots c
WHERE c.customers.id = :id
AND lower(c.dayName) = :dayName
""")
    CalendarAppointmentAvailabilitySlots findSlot(
            @Param("id") Integer id,
            @Param("dayName") String dayName);
}