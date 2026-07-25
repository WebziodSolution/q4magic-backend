package com.q4magic.opportunityPartnerDetails.serviceImpl;

import com.q4magic.common.dto.OpportunityPartnerDetailsDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.Account;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunityPartnerDetails;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.AccountRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.OpportunityPartnerDetailsRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.opportunityPartnerDetails.service.OpportunityPartnerDetailsService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "OpportunityPartnerDetailsService")
public class OpportunityPartnerDetailsServiceImpl implements OpportunityPartnerDetailsService {

    @Autowired
    private OpportunityPartnerDetailsRepository opportunityPartnerDetailsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public OpportunityPartnerDetailsDto getSalesforceOpportunityPartnerId(String id) {
        try {
            OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findBySalesforceOpportunityPartnerId(id);
            if (opportunityPartnerDetails != null) {
                return this.getOpportunityPartnerDetailsById(opportunityPartnerDetails.getId());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OpportunityPartnerDetailsDto> getAllOpportunityPartnerDetails(Integer id) {
        try {
            List<OpportunityPartnerDetails> opportunityPartnerDetailsList = this.opportunityPartnerDetailsRepository.findPartnersByOppId(id);
            List<OpportunityPartnerDetailsDto> opportunityPartnerDetailsDtos = new ArrayList<>();

            if (!opportunityPartnerDetailsList.isEmpty()) {
                for (OpportunityPartnerDetails opportunityPartnerDetails : opportunityPartnerDetailsList) {
                    opportunityPartnerDetailsDtos.add(this.getOpportunityPartnerDetailsById(opportunityPartnerDetails.getId()));
                }
            }
            return opportunityPartnerDetailsDtos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityPartnerDetailsDto getOpportunityPartnerDetailsById(Integer id) {
        try {
            OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("OpportunityPartnerDetails not found with id: " + id));
            OpportunityPartnerDetailsDto dto = new OpportunityPartnerDetailsDto();
            Account account = this.accountRepository.findById(Integer.parseInt(opportunityPartnerDetails.getAccountId())).orElseThrow(() -> new RuntimeException("Account not found"));
            dto.setId(opportunityPartnerDetails.getId());
            dto.setSalesforceOpportunityPartnerId(opportunityPartnerDetails.getSalesforceOpportunityPartnerId());
            dto.setOpportunityId(opportunityPartnerDetails.getOpportunities().getId());
            dto.setAccountToId(opportunityPartnerDetails.getAccountToId());
            dto.setAccountId(opportunityPartnerDetails.getAccountId());
            dto.setAccountName(account.getAccountName());
            dto.setRole(opportunityPartnerDetails.getRole());
            dto.setIsDeleted(opportunityPartnerDetails.getIsDeleted());
            dto.setIsPrimary(opportunityPartnerDetails.getIsPrimary());
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityPartnerDetailsDto createOpportunityPartnerDetails(OpportunityPartnerDetailsDto dto, Boolean isSyncToSalesforce) {
        try {
            OpportunityPartnerDetails opportunityPartnerDetails = new OpportunityPartnerDetails();
            Opportunities opportunities = opportunitiesRepository.findById(dto.getOpportunityId())
                    .orElseThrow(() -> new RuntimeException("Opportunities not found with id: " + dto.getOpportunityId()));
            opportunityPartnerDetails.setOpportunities(opportunities);
            opportunityPartnerDetails.setSalesforceOpportunityPartnerId(dto.getSalesforceOpportunityPartnerId());
            opportunityPartnerDetails.setAccountToId(dto.getAccountToId());
            opportunityPartnerDetails.setAccountId(dto.getAccountId());
            opportunityPartnerDetails.setRole(dto.getRole());
            opportunityPartnerDetails.setIsPrimary(dto.getIsPrimary());
            opportunityPartnerDetails.setIsDeleted(false);
            OpportunityPartnerDetails savedEntity = opportunityPartnerDetailsRepository.save(opportunityPartnerDetails);
            dto.setId(savedEntity.getId());
            if (isSyncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(savedEntity.getId());
                syncRecordsQueueDto.setSubject("OpportunitiesPartner");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(dto.getCreatedBy());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityPartnerDetailsDto updateOpportunityPartnerDetails(Integer id, OpportunityPartnerDetailsDto dto, Boolean isSyncToSalesforce) {
        try {
            OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("OpportunityPartnerDetails not found with id: " + id));
            Opportunities opportunities = opportunitiesRepository.findById(dto.getOpportunityId())
                    .orElseThrow(() -> new RuntimeException("Opportunities not found with id: " + dto.getOpportunityId()));
            opportunityPartnerDetails.setOpportunities(opportunities);
            opportunityPartnerDetails.setSalesforceOpportunityPartnerId(dto.getSalesforceOpportunityPartnerId());
            opportunityPartnerDetails.setAccountToId(dto.getAccountToId());
            opportunityPartnerDetails.setAccountId(dto.getAccountId());
            opportunityPartnerDetails.setRole(dto.getRole());
            opportunityPartnerDetails.setIsPrimary(dto.getIsPrimary());
            opportunityPartnerDetails.setIsDeleted(false);
            OpportunityPartnerDetails savedEntity = opportunityPartnerDetailsRepository.save(opportunityPartnerDetails);
            if (isSyncToSalesforce && opportunityPartnerDetails.getSalesforceOpportunityPartnerId() != null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(id, dto.getCreatedBy());
                if (syncRecordsQueue == null) {
                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                    syncRecordsQueueDto.setSubjectId(id);
                    syncRecordsQueueDto.setSubject("OpportunitiesPartner");
                    syncRecordsQueueDto.setOperationType("UPDATE");
                    syncRecordsQueueDto.setSyncType("PUSH");
                    syncRecordsQueueDto.setDeleted(false);
                    syncRecordsQueueDto.setCreatedBy(dto.getCreatedBy());
                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                }
            }
            dto.setId(savedEntity.getId());
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOpportunityPartnerDetails(Integer id, Boolean isSyncToSalesforce, Integer createdBy) {
        try {
            OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("OpportunityPartnerDetails not found with id: " + id));
            if (!isSyncToSalesforce || opportunityPartnerDetails.getSalesforceOpportunityPartnerId() == null) {
                SyncRecordsQueue existingSyncRecord = this.syncRecordsQueueRepository.findBySubjectIdAndSubject(id, "OpportunitiesPartner", createdBy);
                if (existingSyncRecord != null) {
                    this.syncRecordsQueueService.deleteSyncRecord(existingSyncRecord.getId());
                }
                this.opportunityPartnerDetailsRepository.delete(opportunityPartnerDetails);
            } else {
                opportunityPartnerDetails.setIsDeleted(true);
                this.opportunityPartnerDetailsRepository.save(opportunityPartnerDetails);

                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(id);
                syncRecordsQueueDto.setSubject("OpportunitiesPartner");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(createdBy);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
