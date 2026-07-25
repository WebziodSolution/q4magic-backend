package com.q4magic.syncRecordsQueue.service;

import com.q4magic.common.dto.SyncRecordsQueueDto;

import java.util.List;

public interface SyncRecordsQueueService {
    List<SyncRecordsQueueDto> getAllDeleted(Integer userId);

    SyncRecordsQueueDto findBySubjectId(Integer subjectId, Integer userId);

    List<SyncRecordsQueueDto> getAllSyncRecordsQueue(Integer id);

    SyncRecordsQueueDto getSyncRecordById(Integer id);

    SyncRecordsQueueDto createSyncRecord(SyncRecordsQueueDto syncRecord);

    SyncRecordsQueueDto updateSyncRecord(Integer id, SyncRecordsQueueDto syncRecord);

    void deleteSyncRecord(Integer id);

}
