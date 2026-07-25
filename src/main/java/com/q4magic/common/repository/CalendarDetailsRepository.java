package com.q4magic.common.repository;

import com.q4magic.common.models.CalendarDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface CalendarDetailsRepository extends JpaRepository<CalendarDetails, Integer> {

    // Get all details for a given calendar id
    @Query("select cd from CalendarDetails cd where cd.calendar.id = :calId")
    List<CalendarDetails> findCalendarDetailsList(@Param("calId") Integer calId);

    // Get single detail by calendar id + type
    @Query("select cd from CalendarDetails cd where cd.calendar.id = :id and cd.caldType = :caldType")
    CalendarDetails findByCalId(@Param("id") Integer id,
                                @Param("caldType") String caldType);

    // Count records for a given calendar id
    @Query("select count(cd) from CalendarDetails cd where cd.calendar.id = :id")
    int findCountRecords(@Param("id") Integer id);

    // Get all details where calendar id is in a list
    @Query("select cd from CalendarDetails cd where cd.calendar.id in :ids")
    List<CalendarDetails> findCDsWithParentIdList(@Param("ids") List<Integer> ids);

    @Query("select cd from CalendarDetails cd where cd.caldSycId = :id and cd.caldType = :caldType")
    CalendarDetails findSycId(@Param("id") String id, @Param("caldType") String caldType);

}