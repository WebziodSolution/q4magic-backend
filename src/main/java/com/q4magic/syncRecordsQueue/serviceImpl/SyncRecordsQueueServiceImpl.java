package com.q4magic.syncRecordsQueue.serviceImpl;

import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "syncRecordsQueueService")
public class SyncRecordsQueueServiceImpl implements SyncRecordsQueueService {

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<SyncRecordsQueueDto> getAllDeleted(Integer userId) {
        try {
            List<SyncRecordsQueue> syncRecordsQueueList = this.syncRecordsQueueRepository.getAllDeleted(userId);
            List<SyncRecordsQueueDto> syncRecordsQueueDtoList = new ArrayList<>();
            if (!syncRecordsQueueList.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueueList) {
                    syncRecordsQueueDtoList.add(this.getSyncRecordById(syncRecordsQueue.getId()));
                }
            }
            return syncRecordsQueueDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SyncRecordsQueueDto findBySubjectId(Integer subjectId, Integer userId) {
        try {
            SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(subjectId, userId);
            if (syncRecordsQueue != null) {
                return this.getSyncRecordById(syncRecordsQueue.getId());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SyncRecordsQueueDto> getAllSyncRecordsQueue(Integer userId) {
        try {
            List<SyncRecordsQueue> syncRecordsQueueList = this.syncRecordsQueueRepository.findByUserId(userId);
            List<SyncRecordsQueueDto> syncRecordsQueueDtoList = new ArrayList<>();
            if (!syncRecordsQueueList.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueueList) {
                    syncRecordsQueueDtoList.add(this.getSyncRecordById(syncRecordsQueue.getId()));
                }
            }
            return syncRecordsQueueDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SyncRecordsQueueDto getSyncRecordById(Integer id) {
        try {
            SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sync record not found with id: " + id));
            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
            syncRecordsQueueDto.setCreatedBy(syncRecordsQueue.getCustomers().getId());
            syncRecordsQueueDto.setCreatedByName(syncRecordsQueue.getCustomers().getUsername());
            if (syncRecordsQueue.getDate() != null) {
                syncRecordsQueueDto.setDate(this.commonService.convertDateToString(syncRecordsQueue.getDate()));
            }
            BeanUtils.copyProperties(syncRecordsQueue, syncRecordsQueueDto);
            return syncRecordsQueueDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SyncRecordsQueueDto createSyncRecord(SyncRecordsQueueDto syncRecord) {
        try {
            SyncRecordsQueue syncRecordsQueue = new SyncRecordsQueue();
            Customers customer = this.customersRepository.findById(syncRecord.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + syncRecord.getCreatedBy()));
            if (customer.getCustomers() != null) {
                customer = this.customersRepository.findById(customer.getCustomers().getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
            syncRecordsQueue.setCustomers(customer);
            Date currentDate = new Date();
            syncRecordsQueue.setDate(currentDate);
            BeanUtils.copyProperties(syncRecord, syncRecordsQueue, "id", "date");
            this.syncRecordsQueueRepository.save(syncRecordsQueue);
            return syncRecord;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SyncRecordsQueueDto updateSyncRecord(Integer id, SyncRecordsQueueDto syncRecord) {
        try {
            SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sync record not found with id: " + id));
            Date currentDate = new Date();
            syncRecordsQueue.setDate(currentDate);
            BeanUtils.copyProperties(syncRecord, syncRecordsQueue, "id", "date");
            this.syncRecordsQueueRepository.save(syncRecordsQueue);
            return syncRecord;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSyncRecord(Integer id) {
        try {
            SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sync record not found with id: " + id));
            this.syncRecordsQueueRepository.delete(syncRecordsQueue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
