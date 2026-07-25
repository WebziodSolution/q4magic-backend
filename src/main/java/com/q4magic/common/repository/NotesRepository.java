package com.q4magic.common.repository;

import com.q4magic.common.models.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotesRepository extends JpaRepository<Notes, Integer> {
    @Query(value = "SELECT m FROM Notes m WHERE m.meetings.id = :id ORDER BY m.id DESC")
    Notes findByMeetingId(@Param("id") Integer id);
}
