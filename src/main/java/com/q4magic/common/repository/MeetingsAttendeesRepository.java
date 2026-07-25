package com.q4magic.common.repository;

import com.q4magic.common.models.MeetingsAttendees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingsAttendeesRepository extends JpaRepository<MeetingsAttendees, Integer> {
    @Query(value = "SELECT m FROM MeetingsAttendees m WHERE m.meetings.id = :id ORDER BY m.id DESC")
    List<MeetingsAttendees> findByMeetingId(@Param("id") Integer id);
}
