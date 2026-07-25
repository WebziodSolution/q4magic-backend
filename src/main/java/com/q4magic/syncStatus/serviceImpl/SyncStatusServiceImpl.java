package com.q4magic.syncStatus.serviceImpl;

import com.q4magic.common.dto.SyncStatusDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.SyncStatus;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.SyncStatusRepository;
import com.q4magic.syncStatus.service.SyncStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "SyncStatusService")
public class SyncStatusServiceImpl implements SyncStatusService {

    @Autowired
    private SyncStatusRepository syncStatusRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public SyncStatusDto findByUserId(Integer id) {
        try {
            SyncStatus syncStatus = this.syncStatusRepository.findByUserId(id);
            if (syncStatus != null) {
                SyncStatusDto syncStatusDto = new SyncStatusDto();
                syncStatusDto.setId(syncStatus.getId());
                syncStatusDto.setStatusMessage(syncStatus.getStatusMessage());
                syncStatusDto.setCustomerId(syncStatus.getCustomers().getId());
                syncStatusDto.setStatus(syncStatus.getStatus());
                return syncStatusDto;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveSyncStatus(SyncStatusDto syncStatusDto) {
        try {
            Integer customerId = syncStatusDto.getCustomerId();
            if (customerId == null) {
                throw new IllegalArgumentException("Customer ID must not be null");
            }

            SyncStatus syncStatus = syncStatusRepository.findByUserId(customerId);
            if (syncStatus == null) {
                syncStatus = new SyncStatus();
            } else if (syncStatus.getId() == null) {
                // If the found entity has no ID, treat as new (or handle accordingly)
                syncStatus = new SyncStatus();
            }

            Customers customers = customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found for id: " + customerId));

            syncStatus.setCustomers(customers);
            syncStatus.setStatusMessage(syncStatusDto.getStatusMessage());
            syncStatus.setStatus(1);
            this.syncStatusRepository.save(syncStatus);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSyncStatus(Integer id) {
        try {
            SyncStatus syncStatus = this.syncStatusRepository.findByUserId(id);
            this.syncStatusRepository.delete(syncStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
