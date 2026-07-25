package com.q4magic.opportunityContact.serviceImpl;

import com.q4magic.common.dto.OpportunityContactDto;
import com.q4magic.common.dto.OpportunityContactNotesDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.Contacts;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunityContact;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.OpportunityContactRepository;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.opportunityContact.service.OpportunityContactService;
import com.q4magic.opportunityContactNotes.service.OpportunityContactNotesService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "OpportunityContactService")
public class OpportunityContactServiceImpl implements OpportunityContactService {

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private OpportunityContactNotesService opportunityContactNotesService;

    @Override
    public List<OpportunityContactDto> getAllOppContact(Integer oppId) {
        try {
            List<OpportunityContact> opportunityContactList =
                    this.opportunityContactRepository.findByOppId(oppId);

            List<OpportunityContactDto> opportunityContactDtoList = new ArrayList<>();

            if (!opportunityContactList.isEmpty()) {
                for (OpportunityContact opportunityContact : opportunityContactList) {

                    OpportunityContactDto dto = new OpportunityContactDto();
                    dto.setId(opportunityContact.getId());
                    if (opportunityContact.getContacts() != null) {
                        dto.setContactId(opportunityContact.getContacts().getId());
                        dto.setContactName(
                                opportunityContact.getContacts().getFirstName() + " "
                                        + opportunityContact.getContacts().getLastName()
                        );
                        dto.setSalesforceContactId(opportunityContact.getSalesforceContactId());
                        dto.setTitle(opportunityContact.getTitle());
                    }
                    dto.setOppId(opportunityContact.getOpportunities().getId());
                    dto.setRole(opportunityContact.getRole());
                    dto.setIsKey(opportunityContact.getIsKey());
                    dto.setSalesforceOpportunityContactId(opportunityContact.getSalesforceOpportunityContactId());
                    List<OpportunityContactNotesDto> opportunityContactNotesDtoList = this.opportunityContactNotesService.findByOpportunityContactId(opportunityContact.getId());
                    dto.setOpportunityContactNotesList(opportunityContactNotesDtoList);
                    opportunityContactDtoList.add(dto);
                }
            }

            // ⭐ FINAL STEP: Sort list so that isKey == true comes FIRST
            opportunityContactDtoList.sort((a, b) -> {
                Boolean keyA = a.getIsKey() != null ? a.getIsKey() : false;
                Boolean keyB = b.getIsKey() != null ? b.getIsKey() : false;
                return keyB.compareTo(keyA);  // true first
            });

            return opportunityContactDtoList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOppContact(Integer createdById, List<OpportunityContactDto> opportunityContactDto, Boolean isSyncToSalesforce) {
        try {
            if (!opportunityContactDto.isEmpty()) {
                for (OpportunityContactDto opportunityContactDto1 : opportunityContactDto) {
                    List<OpportunityContact> opportunityContact = this.opportunityContactRepository.findByOppIdAndContactId(opportunityContactDto1.getOppId(), opportunityContactDto1.getContactId());
                    if (opportunityContact.isEmpty()) {
                        OpportunityContact newOppContact = new OpportunityContact();
                        Contacts contacts = this.contactsRepository.findById(opportunityContactDto1.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                        Opportunities opportunities = this.opportunitiesRepository.findById(opportunityContactDto1.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));

                        newOppContact.setContacts(contacts);
                        newOppContact.setOpportunities(opportunities);
                        newOppContact.setIsDeleted(false);
                        newOppContact.setSalesforceOpportunityContactId(opportunityContactDto1.getSalesforceOpportunityContactId());
                        newOppContact.setSalesforceContactId(opportunityContactDto1.getSalesforceContactId());
                        newOppContact.setRole(opportunityContactDto1.getRole());
                        BeanUtils.copyProperties(opportunityContactDto1, newOppContact, "id", "opportunities", "contacts", "role");
                        this.opportunityContactRepository.save(newOppContact);

                        if (opportunityContactDto1.getOpportunityContactNotesList() != null && !opportunityContactDto1.getOpportunityContactNotesList().isEmpty()) {
                            List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                            for (OpportunityContactNotesDto opportunityContactNotesDto1 : opportunityContactDto1.getOpportunityContactNotesList()) {
                                opportunityContactNotesDto1.setOpportunityContactId(newOppContact.getId());
                                opportunityContactNotesDtoList.add(opportunityContactNotesDto1);
                            }
                            this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
                        }

                        if (isSyncToSalesforce) {
                            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                            syncRecordsQueueDto.setSubjectId(newOppContact.getId());
                            syncRecordsQueueDto.setSubject("OpportunitiesContacts");
                            syncRecordsQueueDto.setOperationType("CREATE");
                            syncRecordsQueueDto.setSyncType("PUSH");
                            syncRecordsQueueDto.setDeleted(false);
                            syncRecordsQueueDto.setCreatedBy(createdById);
                            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                        }
                    } else {
                        for (OpportunityContact opportunityContact1 : opportunityContact) {
                            if (opportunityContact1.getSalesforceOpportunityContactId() != null && opportunityContact1.getSalesforceOpportunityContactId().equals(opportunityContactDto1.getSalesforceOpportunityContactId())) {
                                throw new RuntimeException(opportunityContact1.getContacts().getFirstName() + " " + opportunityContact1.getContacts().getLastName() + "is already exits for this opportunity");
                            } else {
                                OpportunityContact newOppContact = new OpportunityContact();
                                Contacts contacts = this.contactsRepository.findById(opportunityContactDto1.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                                Opportunities opportunities = this.opportunitiesRepository.findById(opportunityContactDto1.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));

                                newOppContact.setContacts(contacts);
                                newOppContact.setOpportunities(opportunities);
                                newOppContact.setIsDeleted(false);
                                newOppContact.setSalesforceOpportunityContactId(opportunityContactDto1.getSalesforceOpportunityContactId());
                                newOppContact.setSalesforceContactId(opportunityContactDto1.getSalesforceContactId());
                                newOppContact.setRole(opportunityContactDto1.getRole());
                                newOppContact.setTitle(opportunityContact1.getTitle());
                                BeanUtils.copyProperties(opportunityContactDto1, newOppContact, "id", "opportunities", "contacts", "role", "title");
                                this.opportunityContactRepository.save(newOppContact);

                                if (opportunityContactDto1.getOpportunityContactNotesList() != null && !opportunityContactDto1.getOpportunityContactNotesList().isEmpty()) {
                                    List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                                    for (OpportunityContactNotesDto opportunityContactNotesDto1 : opportunityContactDto1.getOpportunityContactNotesList()) {
                                        opportunityContactNotesDto1.setOpportunityContactId(newOppContact.getId());
                                        opportunityContactNotesDtoList.add(opportunityContactNotesDto1);
                                    }
                                    this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
                                }

                                if (isSyncToSalesforce) {
                                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                                    syncRecordsQueueDto.setSubjectId(newOppContact.getId());
                                    syncRecordsQueueDto.setSubject("OpportunitiesContacts");
                                    syncRecordsQueueDto.setOperationType("CREATE");
                                    syncRecordsQueueDto.setSyncType("PUSH");
                                    syncRecordsQueueDto.setDeleted(false);
                                    syncRecordsQueueDto.setCreatedBy(createdById);
                                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOppContact(Integer createdById, OpportunityContactDto opportunityContactDto, Boolean isSyncToSalesforce) {
        try {
            OpportunityContact opportunityContact = new OpportunityContact();

            if (opportunityContactDto.getContactId() != null) {
                Contacts contacts = this.contactsRepository.findById(opportunityContactDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                opportunityContact.setContacts(contacts);
            }

            Opportunities opportunities = this.opportunitiesRepository.findById(opportunityContactDto.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));
            opportunityContact.setOpportunities(opportunities);
            opportunityContact.setSalesforceOpportunityContactId(opportunityContactDto.getSalesforceOpportunityContactId());
            opportunityContact.setIsKey(opportunityContactDto.getIsKey());
            opportunityContact.setSalesforceContactId(opportunityContactDto.getSalesforceContactId());
            opportunityContact.setRole(opportunityContactDto.getRole());
            opportunityContact.setIsDeleted(false);
            opportunityContact.setTitle(opportunityContactDto.getTitle());
            this.opportunityContactRepository.save(opportunityContact);

            if (!opportunityContactDto.getOpportunityContactNotesList().isEmpty()) {
                List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                for (OpportunityContactNotesDto opportunityContactNotesDto1 : opportunityContactDto.getOpportunityContactNotesList()) {
                    opportunityContactNotesDto1.setOpportunityContactId(opportunityContact.getId());
                    opportunityContactNotesDtoList.add(opportunityContactNotesDto1);
                }
                this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
            }

            if (isSyncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunityContact.getId());
                syncRecordsQueueDto.setSubject("OpportunitiesProducts");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(createdById);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateOppContact(Integer createdById,
                                 List<OpportunityContactDto> opportunityContactDto,
                                 Boolean isSyncToSalesforce) {
        try {
            if (!opportunityContactDto.isEmpty()) {
                for (OpportunityContactDto dto : opportunityContactDto) {

                    // 1) Load existing record by its primary key (this is present)
                    OpportunityContact opportunityContact =
                            this.opportunityContactRepository.findById(dto.getId())
                                    .orElseThrow(() -> new RuntimeException("Opp contact not found"));

                    // 2) Only update Opportunity if oppId is provided
                    if (dto.getOppId() != null) {
                        Opportunities opportunities =
                                this.opportunitiesRepository.findById(dto.getOppId())
                                        .orElseThrow(() -> new RuntimeException("Opp not found"));
                        opportunityContact.setOpportunities(opportunities);
                    }

                    // 3) Only update Contact if contactId is provided
                    if (dto.getContactId() != null) {
                        Contacts contacts =
                                this.contactsRepository.findById(dto.getContactId())
                                        .orElseThrow(() -> new RuntimeException("Contact not found"));
                        opportunityContact.setContacts(contacts);
                    }

                    // 4) Update isKey if present
                    if (dto.getIsKey() != null) {
                        opportunityContact.setIsKey(dto.getIsKey());
                    }

                    // 5) Always keep it not deleted
                    opportunityContact.setIsDeleted(false);
                    opportunityContact.setRole(dto.getRole());
                    opportunityContact.setTitle(dto.getTitle());
                    this.opportunityContactRepository.save(opportunityContact);

                    if (!dto.getOpportunityContactNotesList().isEmpty()) {
                        List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                        for (OpportunityContactNotesDto opportunityContactNotesDto1 : dto.getOpportunityContactNotesList()) {
                            opportunityContactNotesDto1.setOpportunityContactId(opportunityContact.getId());
                            opportunityContactNotesDtoList.add(opportunityContactNotesDto1);
                        }
                        this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
                    }

                    // 6) Sync to Salesforce only if needed
                    if (Boolean.TRUE.equals(isSyncToSalesforce)
                            && opportunityContact.getSalesforceOpportunityContactId() != null) {

                        SyncRecordsQueue syncRecordsQueue =
                                this.syncRecordsQueueRepository.findBySubjectId(dto.getId(), createdById);

                        if (syncRecordsQueue == null) {
                            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                            syncRecordsQueueDto.setSubjectId(opportunityContact.getId());
                            syncRecordsQueueDto.setSubject("OpportunitiesContacts");
                            syncRecordsQueueDto.setOperationType("UPDATE");
                            syncRecordsQueueDto.setSyncType("PUSH");
                            syncRecordsQueueDto.setDeleted(false);
                            syncRecordsQueueDto.setCreatedBy(createdById);

                            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateOppContact(Integer createdById, Integer id, OpportunityContactDto opportunityContactDto, Boolean isSyncToSalesforce) {
        try {
            OpportunityContact opportunityContact = this.opportunityContactRepository.findById(id).orElseThrow(() -> new RuntimeException("Opp contact not found"));

            Contacts contacts = this.contactsRepository.findById(opportunityContactDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
            opportunityContact.setContacts(contacts);

            opportunityContact.setIsKey(opportunityContactDto.getIsKey());
            opportunityContact.setSalesforceOpportunityContactId(opportunityContactDto.getSalesforceOpportunityContactId());
            opportunityContact.setSalesforceContactId(opportunityContactDto.getSalesforceContactId());
            opportunityContact.setRole(opportunityContactDto.getRole());
            opportunityContact.setTitle(opportunityContactDto.getTitle());
            opportunityContact.setIsDeleted(false);

            this.opportunityContactRepository.save(opportunityContact);

            if (opportunityContactDto.getOpportunityContactNotesList() != null) {
                List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                for (OpportunityContactNotesDto opportunityContactNotesDto1 : opportunityContactDto.getOpportunityContactNotesList()) {
                    opportunityContactNotesDto1.setOpportunityContactId(opportunityContact.getId());
                    opportunityContactNotesDtoList.add(opportunityContactNotesDto1);
                }
                this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
            }

            if (isSyncToSalesforce && opportunityContact.getSalesforceOpportunityContactId() != null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(opportunityContact.getId(), createdById);
                if (syncRecordsQueue == null) {
                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                    syncRecordsQueueDto.setSubjectId(opportunityContact.getId());
                    syncRecordsQueueDto.setSubject("OpportunitiesContacts");
                    syncRecordsQueueDto.setOperationType("UPDATE");
                    syncRecordsQueueDto.setSyncType("PUSH");
                    syncRecordsQueueDto.setDeleted(false);
                    syncRecordsQueueDto.setCreatedBy(createdById);
                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOppContact(Integer createdById, Integer id, Boolean isSyncToSalesforce) {
        try {
            OpportunityContact opportunityContact = this.opportunityContactRepository.findById(id).orElseThrow(() -> new RuntimeException("Opp contact not found"));
//            this.opportunityContactRepository.delete(opportunityContact);
            if (isSyncToSalesforce && opportunityContact.getSalesforceOpportunityContactId() != null) {
                opportunityContact.setIsDeleted(true);
                this.opportunityContactRepository.save(opportunityContact);
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunityContact.getId());
                syncRecordsQueueDto.setSubject("OpportunitiesContacts");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(createdById);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            } else {
                SyncRecordsQueueDto existingSyncRecord = this.syncRecordsQueueService.findBySubjectId(id, createdById);
                if (existingSyncRecord != null) {
                    this.syncRecordsQueueService.deleteSyncRecord(existingSyncRecord.getId());
                }
                this.opportunityContactRepository.delete(opportunityContact);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
