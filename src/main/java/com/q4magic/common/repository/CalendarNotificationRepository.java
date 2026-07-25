package com.q4magic.common.repository;

import com.q4magic.common.models.CalendarNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarNotificationRepository extends JpaRepository<CalendarNotification, Integer> {
    @Query(value = "SELECT c FROM CalendarNotification c WHERE calendar.id = :id")
    List<CalendarNotification> findByCalendarId(@Param("id") Integer id);
}
