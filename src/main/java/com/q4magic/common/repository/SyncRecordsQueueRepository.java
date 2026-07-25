package com.q4magic.common.repository;

import com.q4magic.common.models.SyncRecordsQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncRecordsQueueRepository extends JpaRepository<SyncRecordsQueue, Integer> {
    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.subject = :subject AND s.operationType = :operationType AND s.syncType = :syncType AND s.customers.id = :userId AND s.isDeleted = false AND s.error IS NULL")
    List<SyncRecordsQueue> findBySubjectAndType(String subject, String operationType, String syncType, Integer userId);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.customers.id=:userId")
    List<SyncRecordsQueue> findByUserId(Integer userId);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.customers.id=:userId AND s.subject = :subject AND s.isDeleted = false")
    List<SyncRecordsQueue> findByUserIdAndSubject(Integer userId, String subject);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.customers.id=:userId AND s.subjectId=:subjectId AND s.isDeleted = false")
    SyncRecordsQueue findBySubjectId(Integer subjectId, Integer userId);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.customers.id=:userId AND s.subjectId=:subjectId AND s.subject=:subject AND s.isDeleted = false")
    SyncRecordsQueue findBySubjectIdAndSubject(Integer subjectId, String subject, Integer userId);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.syncType = :syncType AND s.customers.id = :userId AND s.isDeleted = :isDeleted")
    List<SyncRecordsQueue> findByType(@Param("syncType") String syncType,
                                      @Param("userId") Integer userId,
                                      @Param("isDeleted") Boolean isDeleted);

    @Query("SELECT s FROM SyncRecordsQueue s WHERE s.customers.id = :userId AND s.isDeleted = true")
    List<SyncRecordsQueue> getAllDeleted(@Param("userId") Integer userId);
}
