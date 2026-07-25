package com.q4magic.syncStatus.service;

import com.q4magic.common.dto.SyncStatusDto;

public interface SyncStatusService {
    SyncStatusDto findByUserId(Integer id);
    void saveSyncStatus(SyncStatusDto syncStatusDto);
    void deleteSyncStatus(Integer id);
}