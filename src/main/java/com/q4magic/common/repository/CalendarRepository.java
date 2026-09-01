package com.q4magic.common.repository;

import com.q4magic.common.models.Calendar;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface CalendarRepository extends JpaRepository<Calendar, Integer> {

    @Query("select c from Calendar c where c.customers.id = :cusId")
    List<Calendar> findCalendarByCustomerId(@Param("cusId") Integer cusId);

    @Query("select c from Calendar c where c.customers.id = :cusId and c.calStartDateTime = :calStartDateTime")
    List<Calendar> findByCustomerIdAndCalStartDateTime(@Param("cusId") Integer cusId,
                                                      @Param("calStartDateTime") Date calStartDateTime);

    // List calendars for a customer between start and end date/time
    @Query("select c from Calendar c " +
            "where c.customers.id = :cusId " +
            "and c.calStartDateTime >= :calStartDateTime " +
            "and c.calEndDateTime <= :calEndDateTime")
    List<Calendar> findCalendarList(@Param("cusId") Integer cusId,
                                    @Param("calStartDateTime") Date calStartDateTime,
                                    @Param("calEndDateTime") Date calEndDateTime);

    @Query("select c from Calendar c " +
            "where c.customers.id = :cusId " +
            "and c.calCreatedDateTime >= :calStartDateTime " +
            "and c.calCreatedDateTime <= :calEndDateTime")
    List<Calendar> findCalendarListFilter(@Param("cusId") Integer cusId,
                                    @Param("calStartDateTime") Date calStartDateTime,
                                    @Param("calEndDateTime") Date calEndDateTime);

    // Find one calendar by customer and calendar id
    @Query("select c from Calendar c where c.customers.id = :cusId and c.id = :calId")
    Calendar findCalendarById(@Param("calId") Integer calId,
                              @Param("cusId") Integer cusId);

    // Delete one calendar by customer and id
    @Modifying
    @Query("delete from Calendar c where c.customers.id = :cusId and c.id = :calId")
    void deleteByCalId(@Param("calId") Integer calId,
                       @Param("cusId") Integer cusId);

    @Query(
            value = "select * from calendar " +
                    "where cus_id = :cusId " +
                    "and cal_title = :calTitle " +
                    "and DATE(cal_start_date_time) = DATE(:calStartDateTime) " +
                    "and DATE(cal_end_date_time) = DATE(:calEndDateTime)",
            nativeQuery = true
    )
    Calendar findData(
            @Param("cusId") Integer cusId,
            @Param("calTitle") String calTitle,
            @Param("calStartDateTime") String calStartDateTime,
            @Param("calEndDateTime") String calEndDateTime
    );


    @Query(value = "select * from calendar where cus_id=:cusId and id not in (:calIdList) and DATE(cal_start_date_time)>=:currentDate and cal_event_reminder != 'reminder'", nativeQuery = true)
    List<Calendar> findListNotCalendarData(Integer cusId, List<Integer> calIdList, String currentDate);

    // Calendars of a given type (via calendar_details)
    @Query("select distinct c.id from Calendar c " +
            "join CalendarDetails cd on cd.calendar.id = c.id " +
            "where c.customers.id = :cusId " +
            "and cd.caldType = :caldType " +
            "and cd.caldSycId is not null and cd.caldSycId <> '' " +
            "and c.calEventReminder <> 'reminder'")
    List<Integer> findTypeCalendarList(@Param("cusId") Integer cusId,
                                       @Param("caldType") String caldType);


    @Query(value = "select * from calendar where cus_id=:cusId and DATE(cal_start_date_time)>=:currentDate and cal_event_reminder != 'reminder'", nativeQuery = true)
    List<Calendar> findListNotCalendarDataAll(Integer cusId, String currentDate);

    // Bookings for a specific day (service should pass startOfDay and endOfDay)

    @Query("""
SELECT c
FROM Calendar c
WHERE c.customers.id = :cusId
AND FUNCTION('DATE', c.calStartDateTime) = :slotDateTime
""")
    List<Calendar> findBookList(
            @Param("cusId") Integer cusId,
            @Param("slotDateTime") String slotDateTime);

//    @Query("select c from Calendar c " +
//            "where c.customers.id = :cusId " +
//            "and c.calStartDateTime >= :dayStart and c.calStartDateTime < :dayEnd")
//    List<Calendar> findBookList(@Param("cusId") Integer cusId,
//                                @Param("dayStart") Date dayStart,
//                                @Param("dayEnd") Date dayEnd);

    // Find all linked cal_id (by id or parent)
    @Query("select distinct c.id from Calendar c " +
            "where c.id = :calId or c.calParentId = :calId")
    List<Integer> findIds(@Param("calId") Integer calId);

    // Delete calendars for a customer by cal_id / parent combination
    @Modifying
    @Query("delete from Calendar c " +
            "where c.customers.id = :cusId " +
            "and (c.id in (:calId, :calParentId) or c.calParentId in (:calId, :calParentId))")
    void deleteByCalIdWithParentId(@Param("calId") Integer calId,
                                   @Param("calParentId") Integer calParentId,
                                   @Param("cusId") Integer cusId);

    // Delete calendars for a customer where id or parent = calId
    @Modifying
    @Query("delete from Calendar c " +
            "where c.customers.id = :cusId " +
            "and (c.id = :calId or c.calParentId = :calId)")
    void deleteByCalIdWithOutParentId(@Param("calId") Integer calId,
                                      @Param("cusId") Integer cusId);

    // Get children calendars for a parent id
    @Query("select c from Calendar c where c.customers.id = :cusId and c.calParentId = :calId")
    List<Calendar> findCalendarListByParentId(@Param("cusId") Integer cusId,
                                              @Param("calId") Integer calId);

    // Get all calendars by cal_id/parent id combo for a customer
    @Query("select distinct c from Calendar c " +
            "where c.customers.id = :cusId " +
            "and (c.id in (:calId, :calParentId) or c.calParentId in (:calId, :calParentId))")
    List<Calendar> findAllDataByCalIdAndParentId(@Param("cusId") Integer cusId,
                                                 @Param("calId") Integer calId,
                                                 @Param("calParentId") Integer calParentId);

    // Get all calendars by id or parent for a customer
    @Query("select distinct c from Calendar c " +
            "where c.customers.id = :cusId " +
            "and (c.id = :calId or c.calParentId = :calId)")
    List<Calendar> findAllDataByCalIdWithOutParentId(@Param("cusId") Integer cusId,
                                                     @Param("calId") Integer calId);

    @Query(value = "select id from calendar where cus_id = :cusId AND (id in (:calId, :calParentId) OR cal_parent_id in (:calId, :calParentId)) group by id", nativeQuery = true)
    List<Integer> findIdsAndParentId(Integer cusId, Integer calId, Integer calParentId);

    @Query(value = "SELECT * FROM calendar WHERE cus_id = :cusId AND cal_start_date_time > :currentDate ORDER BY cal_start_date_time", nativeQuery = true)
    List<Calendar> findUpcomingAppointmentList(Integer cusId, Date currentDate, Pageable pageable);
}
