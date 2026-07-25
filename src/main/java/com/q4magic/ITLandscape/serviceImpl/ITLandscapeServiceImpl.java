package com.q4magic.ITLandscape.serviceImpl;

import com.q4magic.ITLandscape.service.ITLandscapeService;
import com.q4magic.common.dto.ITLandscapeDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.ITLandscape;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.ITLandscapeRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "ITLandscapeService")
public class ITLandscapeServiceImpl implements ITLandscapeService {

    @Autowired
    private ITLandscapeRepository itLandscapeRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Override
    public List<ITLandscapeDto> getAllITLandscapesByOppId(String oppId) {
        try {
            List<ITLandscape> itLandscapes = this.itLandscapeRepository.findBySalesforceOpportunityId(oppId);
            List<ITLandscapeDto> itLandscapeDtoList = new ArrayList<>();
            if (!itLandscapes.isEmpty()) {
                for (ITLandscape itLandscape : itLandscapes) {
                    itLandscapeDtoList.add(this.getITLandscapeById(itLandscape.getId()));
                }
            }
            return itLandscapeDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ITLandscapeDto getITLandscapeById(Integer id) {
        try {
            ITLandscape itLandscape = this.itLandscapeRepository.findById(id).orElseThrow(() -> new RuntimeException("IT Landscape not found"));
            ITLandscapeDto itLandscapeDto = new ITLandscapeDto();
            itLandscapeDto.setCustomerId(itLandscape.getCustomers().getId());
            itLandscapeDto.setOpportunityId(itLandscape.getOpportunities().getId());
            BeanUtils.copyProperties(itLandscape, itLandscapeDto);
            return itLandscapeDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ITLandscapeDto createITLandscape(ITLandscapeDto itLandscapeDto, Boolean syncSalesforce) {
        try {
            Customers customers = this.customersRepository.findById(itLandscapeDto.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found"));
            ITLandscape itLandscape = new ITLandscape();
            itLandscape.setCustomers(customers);
            Opportunities opportunities = null;
            if (itLandscapeDto.getOpportunityId() != null) {
                opportunities = this.opportunitiesRepository.findById(itLandscapeDto.getOpportunityId()).orElseThrow(() -> new RuntimeException("Opportunity not found"));
                itLandscape.setOpportunities(opportunities);
            }
            itLandscape.setIsDeleted(false);
            BeanUtils.copyProperties(itLandscapeDto, itLandscape, "id", "isDeleted");

            ITLandscape savedITLandscape = this.itLandscapeRepository.save(itLandscape);
            BeanUtils.copyProperties(savedITLandscape, itLandscapeDto);

            if (syncSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunities != null ? opportunities.getId() : null);
                syncRecordsQueueDto.setSubject("Competitor");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(itLandscapeDto.getCustomerId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return itLandscapeDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ITLandscapeDto updateITLandscape(Integer id, ITLandscapeDto itLandscapeDto, Boolean syncSalesforce) {
        try {
            ITLandscape itLandscape = this.itLandscapeRepository.findById(id).orElseThrow(() -> new RuntimeException("IT Landscape not found"));
            Customers customers = this.customersRepository.findById(itLandscapeDto.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found"));
            itLandscape.setCustomers(customers);

            Opportunities opportunities = this.opportunitiesRepository.findById(itLandscapeDto.getOpportunityId()).orElseThrow(() -> new RuntimeException("Opportunity not found"));
            itLandscape.setOpportunities(opportunities);
            itLandscape.setIsDeleted(false);

            BeanUtils.copyProperties(itLandscapeDto, itLandscape, "id", "isDeleted");
            ITLandscape savedITLandscape = this.itLandscapeRepository.save(itLandscape);
            BeanUtils.copyProperties(savedITLandscape, itLandscapeDto);

            if (syncSalesforce && opportunities.getSalesforceOpportunityId() != null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(id, itLandscape.getCustomers().getId());
                if (syncRecordsQueue == null) {
                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                    syncRecordsQueueDto.setSubjectId(opportunities.getId());
                    syncRecordsQueueDto.setSubject("Competitor");
                    syncRecordsQueueDto.setOperationType("UPDATE");
                    syncRecordsQueueDto.setSyncType("PUSH");
                    syncRecordsQueueDto.setDeleted(false);
                    syncRecordsQueueDto.setCreatedBy(itLandscape.getCustomers().getId());
                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                }
            }
            return itLandscapeDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteITLandscape(Integer id, Boolean syncSalesforce) {
        try {
            ITLandscape itLandscape = this.itLandscapeRepository.findById(id).orElseThrow(() -> new RuntimeException("IT Landscape not found"));
            if (!syncSalesforce || itLandscape.getSalesforceCompetitorId() == null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(id, itLandscape.getCustomers().getId());
                if (syncRecordsQueue != null) {
                    this.syncRecordsQueueRepository.delete(syncRecordsQueue);
                }
                this.itLandscapeRepository.delete(itLandscape);
            } else {
                itLandscape.setIsDeleted(true);
                this.itLandscapeRepository.save(itLandscape);

                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(itLandscape.getOpportunities().getId());
                syncRecordsQueueDto.setSubject("Competitor");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(itLandscape.getCustomers().getId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
