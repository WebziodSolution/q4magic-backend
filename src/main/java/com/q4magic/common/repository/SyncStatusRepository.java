package com.q4magic.common.repository;

import com.q4magic.common.models.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncStatusRepository extends JpaRepository<SyncStatus, Integer> {
    @Query("SELECT s FROM SyncStatus s WHERE s.customers.id=:id")
    SyncStatus findByUserId(Integer id);
}
