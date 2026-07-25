package com.q4magic.common.salesforce.syncToQ4magic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.q4magic.ITLandscape.service.ITLandscapeService;
import com.q4magic.account.service.AccountService;
import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.salesforce.account.service.SalesforceAccountService;
import com.q4magic.common.salesforce.competitor.service.SalesforceCompetitorService;
import com.q4magic.common.salesforce.contacts.service.SalesforceContactsService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunitiesService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityProductService;
import com.q4magic.common.salesforce.opportunityStage.service.SalesforceOpportunityStageService;
import com.q4magic.common.salesforce.products.service.SalesforceProductsService;
import com.q4magic.contacts.service.ContactsService;
import com.q4magic.oportunities.service.OpportunitiesService;
import com.q4magic.opportunityContact.service.OpportunityContactService;
import com.q4magic.opportunityPartnerDetails.service.OpportunityPartnerDetailsService;
import com.q4magic.opportunityProducts.service.OpportunityProductsService;
import com.q4magic.products.service.ProductService;
import com.q4magic.salesStage.service.SalesStageService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "SyncToQ4MagicService")
public class SyncToQ4MagicServiceImpl implements SyncToQ4MagicService {

    @Autowired
    private SalesforceAccountService salesforceAccountService;

    @Autowired
    private SalesforceOpportunitiesService salesforceOpportunitiesService;

    @Autowired
    private SalesforceContactsService salesforceContactsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OpportunitiesService opportunitiesService;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private OpportunityContactService opportunityContactService;

    @Autowired
    private OpportunityPartnerDetailsService opportunityPartnerDetailsService;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SalesforceCompetitorService salesforceCompetitorService;

    @Autowired
    private ITLandscapeRepository itLandscapeRepository;

    @Autowired
    private ITLandscapeService itLandscapeService;

    @Autowired
    private SalesStagesRepository salesStagesRepository;

    @Autowired
    private SalesforceOpportunityStageService salesforceOpportunityStageService;

    @Autowired
    private SalesforceProductsService salesforceProductsService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SalesStageService salesStageService;

    @Autowired
    private SalesforceOpportunityProductService salesforceOpportunityProductService;

    @Autowired
    private OpportunityProductsRepository opportunityProductsRepository;

    @Autowired
    private OpportunityProductsService opportunityProductsService;

    @Override
    public Map<String, Object> syncAccounts(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = this.salesforceAccountService.getAllAccounts(accessToken, instanceUrl);
            if (result.get("error") != null) {
                saveErrorQueue(result, "GET", userId, "Account");

                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response;
            }

            if (result != null && !result.isEmpty()) {
                Object recordsObj = result.get("records");
                if (recordsObj instanceof List<?>) {
                    List<Map<String, Object>> records = (List<Map<String, Object>>) recordsObj;

                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");
                        String name = (String) record.get("Name");
                        String phone = (String) record.get("Phone");
                        String website = (String) record.get("Website");
                        String logo = (String) record.get("PhotoUrl");

                        AccountDto accountDto = new AccountDto();
                        accountDto.setAccountName(name);
                        accountDto.setPhone(phone);
                        accountDto.setSalesforceAccountId(id);
                        accountDto.setCrmId(1);
                        accountDto.setCreatedBy(userId);
                        accountDto.setLogo(instanceUrl + logo);
                        accountDto.setLink(website);
                        accountDto.setCompanyName(name);

                        Account isExist = this.accountRepository.findBySalesforceAccountId(id);
                        if (isExist == null) {
                            this.accountService.createAccount(accountDto, false, false);
                        } else {
                            this.accountService.updateAccount(isExist.getId(), accountDto, false, false);
                        }
                    }
                } else {
                    response.put("error", "Invalid response format: 'records' is not a List");
                    throw new RuntimeException("Invalid response format: 'records' is not a List");
                }
            }

            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
            syncRecordsQueueDto.setSubject("Account");
            syncRecordsQueueDto.setOperationType("GET");
            syncRecordsQueueDto.setSyncType("PULL");
            syncRecordsQueueDto.setCreatedBy(userId);
            syncRecordsQueueDto.setDeleted(true);
            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            response.put("message", "Accounts synced successfully");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunity(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        Map<String, Object> response = new HashMap<>();
        try {
            // 1) Get Opportunities and Partners from Salesforce
            Map<String, Object> result = this.salesforceOpportunitiesService.getAllOpportunities(accessToken, instanceUrl);

            if (result.get("error") != null) {
                // store in error queue and return error response
                saveErrorQueue(result, "GET", userId, "Opportunities");

                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records =
                    (List<Map<String, Object>>) result.get("opportunities");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> partnersWrapper =
                    (List<Map<String, Object>>) result.get("partners");

            if (records != null && !records.isEmpty()) {

                for (Map<String, Object> record : records) {
                    String opportunityId = (String) record.get("Id");
                    String opportunityName = (String) record.get("Name");
                    Double amount = record.get("Amount") != null ? ((Number) record.get("Amount")).doubleValue() : null;
                    String stageName = (String) record.get("StageName");
                    String status = ((String) record.get("StageName")).equals("Closed Won") ? "Won" : "Pipeline";
                    String closeDate = (String) record.get("CloseDate");
                    String nextStep = (String) record.get("NextStep");
                    String accountId = Objects.toString(record.get("AccountId"), null);

                    // ---------------------------------------------------------------------
                    // 1.a) Fetch all Opportunity Contacts from Salesforce for this opp
                    // ---------------------------------------------------------------------
                    List<Map<String, Object>> allOppContacts = new ArrayList<>();
                    String nextRecordsUrl = null;

                    do {
                        Map<String, Object> listOpportunityContacts =
                                this.salesforceOpportunitiesService.listOpportunityContacts(
                                        instanceUrl,
                                        accessToken,
                                        opportunityId,
                                        2000,
                                        nextRecordsUrl
                                );

                        if (Boolean.TRUE.equals(listOpportunityContacts.get("error"))) {
                            // if error, break; no contacts for this opp
                            break;
                        }

                        @SuppressWarnings("unchecked")
                        Map<String, Object> contactsBody =
                                (Map<String, Object>) listOpportunityContacts.get("result");

                        if (contactsBody == null) {
                            break;
                        }

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> batchRecords =
                                (List<Map<String, Object>>) contactsBody.get("records");

                        if (batchRecords != null) {
                            allOppContacts.addAll(batchRecords);
                        }

                        Boolean done = (Boolean) contactsBody.get("done");
                        if (Boolean.FALSE.equals(done)) {
                            nextRecordsUrl = (String) contactsBody.get("nextRecordsUrl");
                        } else {
                            nextRecordsUrl = null;
                        }
                    } while (nextRecordsUrl != null && !nextRecordsUrl.isEmpty());

                    // ---------------------------------------------------------------------
                    // 2) Map Salesforce Opportunity -> OpportunitiesDto
                    // ---------------------------------------------------------------------
                    OpportunitiesDto opportunitiesDto = new OpportunitiesDto();
                    opportunitiesDto.setOpportunity(opportunityName);
                    opportunitiesDto.setListPrice(amount != null ? amount.intValue() : 0);
                    opportunitiesDto.setDiscountPercentage(0);
                    opportunitiesDto.setDealAmount(amount != null ? amount.intValue() : 0);
                    opportunitiesDto.setSalesStage(stageName);
                    opportunitiesDto.setCloseDate(closeDate);
                    opportunitiesDto.setNextSteps(nextStep);
                    opportunitiesDto.setSalesforceOpportunityId(opportunityId);
                    opportunitiesDto.setCreatedBy(userId);
                    opportunitiesDto.setStatus(status);

                    if (accountId != null) {
                        Account account = this.accountRepository.findBySalesforceAccountId(accountId);
                        opportunitiesDto.setAccountId(account != null ? account.getId() : null);
                    } else {
                        opportunitiesDto.setAccountId(null);
                    }

                    // ---------------------------------------------------------------------
                    // 3) UPSERT Opportunity (create if not exists, else update)
                    // ---------------------------------------------------------------------
                    Opportunities existingOpportunity =
                            this.opportunitiesRepository.findBySalesforceOpportunityId(opportunityId);

                    OpportunitiesDto savedOppDto;

                    // Skip Closed Lost if you want:
                    if (!"Closed Lost".equals(stageName)) {

                        if (existingOpportunity == null) {
                            // CREATE
                            savedOppDto = this.opportunitiesService.createOpportunity(opportunitiesDto, false);
                        } else {
                            // Keep existing MEDDIC text fields
                            opportunitiesDto.setWhyDoAnything(existingOpportunity.getWhyDoAnything());
                            opportunitiesDto.setBusinessValue(existingOpportunity.getBusinessValue());
                            opportunitiesDto.setCurrentEnvironment(existingOpportunity.getCurrentEnvironment());
                            opportunitiesDto.setDecisionMap(existingOpportunity.getDecisionMap());
                            opportunitiesDto.setDecisionCriteria(existingOpportunity.getDecisionCriteria());

                            // UPDATE
                            savedOppDto = this.opportunitiesService.updateOpportunity(
                                    existingOpportunity.getId(),
                                    opportunitiesDto,
                                    false
                            );
                        }

                        Integer oppDbId = savedOppDto.getId();  // use this ID for partners and contacts

                        // -----------------------------------------------------------------
                        // 4) UPSERT Opportunity Partners
                        // -----------------------------------------------------------------
                        if (partnersWrapper != null && !partnersWrapper.isEmpty()) {
                            for (Map<String, Object> partner : partnersWrapper) {
                                // Only partners belonging to this Opportunity
                                if (!opportunityId.equals(partner.get("OpportunityId"))) {
                                    continue;
                                }

                                String partnerAccountToId = (String) partner.get("AccountToId");
                                if (partnerAccountToId == null) {
                                    continue;
                                }

                                Account account = this.accountRepository.findBySalesforceAccountId(partnerAccountToId);

                                OpportunityPartnerDetailsDto partnerDto = new OpportunityPartnerDetailsDto();
                                partnerDto.setOpportunityId(oppDbId); // link to local opp
                                partnerDto.setAccountToId(partnerAccountToId);
                                partnerDto.setSalesforceOpportunityPartnerId((String) partner.get("Id"));
                                partnerDto.setRole((String) partner.get("Role"));
                                partnerDto.setIsPrimary((Boolean) partner.get("IsPrimary"));
                                partnerDto.setAccountId(account != null ? account.getId().toString() : null);

                                OpportunityPartnerDetailsDto existingPartner =
                                        this.opportunityPartnerDetailsService
                                                .getSalesforceOpportunityPartnerId(
                                                        partnerDto.getSalesforceOpportunityPartnerId()
                                                );

                                if (existingPartner != null) {
                                    // UPDATE
                                    this.opportunityPartnerDetailsService.updateOpportunityPartnerDetails(
                                            existingPartner.getId(),
                                            partnerDto,
                                            false
                                    );
                                } else {
                                    // CREATE
                                    this.opportunityPartnerDetailsService.createOpportunityPartnerDetails(
                                            partnerDto,
                                            false
                                    );
                                }
                            }
                        }

                        // -----------------------------------------------------------------
                        // 5) UPSERT Opportunity Contacts
                        // -----------------------------------------------------------------
                        if (allOppContacts != null && !allOppContacts.isEmpty()) {
                            for (Map<String, Object> oppContact : allOppContacts) {
                                String opportunityContactId = (String) oppContact.get("Id");        // OpportunityContactRole Id
                                String contactId = (String) oppContact.get("ContactId");
                                String role = (String) oppContact.get("Role");
                                Boolean isPrimary = Boolean.TRUE.equals(oppContact.get("IsPrimary"));

                                OpportunityContactDto opportunityContactDto = new OpportunityContactDto();
                                opportunityContactDto.setRole(role);
                                opportunityContactDto.setIsKey(isPrimary);
                                opportunityContactDto.setSalesforceOpportunityContactId(opportunityContactId);
                                opportunityContactDto.setSalesforceContactId(contactId);
                                opportunityContactDto.setOppId(oppDbId);  // link to local opp

                                // Map local Contact if it exists
                                Contacts contactEntity = this.contactsRepository.findBySalesforceContactId(contactId);
                                if (contactEntity != null && contactEntity.getId() != null) {
                                    opportunityContactDto.setContactId(contactEntity.getId());
                                }

                                // Find existing OpportunityContact by Salesforce OppContact Id
                                List<OpportunityContact> existingOppContacts =
                                        this.opportunityContactRepository
                                                .findBySalesforceOpportunityContactId(opportunityContactId);

                                if (existingOppContacts != null && !existingOppContacts.isEmpty()) {
                                    // Update first matching record
                                    OpportunityContact existingOppContact = existingOppContacts.get(0);
                                    this.opportunityContactService.updateOppContact(
                                            userId,
                                            existingOppContact.getId(),
                                            opportunityContactDto,
                                            false
                                    );
                                } else {
                                    // Create new
                                    this.opportunityContactService.addOppContact(
                                            userId,
                                            opportunityContactDto,
                                            false
                                    );
                                }
                            }
                        }
                    } // end if not Closed Lost
                } // end for each opportunity
            }

            // -------------------------------------------------------------------------
            // 6) Create SyncRecordsQueue entries
            // -------------------------------------------------------------------------
            SyncRecordsQueueDto opportunitiesQueue = new SyncRecordsQueueDto();
            opportunitiesQueue.setSubject("Opportunities");
            opportunitiesQueue.setOperationType("GET");
            opportunitiesQueue.setSyncType("PULL");
            opportunitiesQueue.setCreatedBy(userId);
            opportunitiesQueue.setDeleted(true);
            this.syncRecordsQueueService.createSyncRecord(opportunitiesQueue);

            SyncRecordsQueueDto partnersQueue = new SyncRecordsQueueDto();
            partnersQueue.setSubject("OpportunityPartners");
            partnersQueue.setOperationType("GET");
            partnersQueue.setSyncType("PULL");
            partnersQueue.setCreatedBy(userId);
            partnersQueue.setDeleted(true);
            this.syncRecordsQueueService.createSyncRecord(partnersQueue);

            response.put("message", "Opportunities synced successfully");
            response.put("status", "success");
            response.put("success", true);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("status", 500);
            response.put("error", e.getClass().getSimpleName());
            response.put("message", e.getMessage());
            return response;
        }
    }

    @Override
    public Map<String, Object> syncContacts(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = this.salesforceContactsService.getAllContacts(accessToken, instanceUrl);
            if (result.get("error") != null) {
                saveErrorQueue(result, "GET", userId, "Contact");

                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response; // instead of throw
            }
            if (!result.isEmpty()) {
                Object recordsObj = result.get("records");
                if (recordsObj instanceof List<?>) {
                    List<Map<String, Object>> records = (List<Map<String, Object>>) recordsObj;
                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");

                        Map<String, Object> res = this.salesforceContactsService.getContactDetails(id, accessToken, instanceUrl);
                        if (res.containsKey("error")) {
                            response.put("success", false);
                            response.put("error", res.get("error"));
                            response.put("status", res.get("status"));
                            response.put("message", res.get("message"));
                            return response;
                        }
                        Map<String, Object> newContact = (Map<String, Object>) res.get("contact");
                        String firstName = (String) newContact.get("FirstName");
                        String lastName = (String) newContact.get("LastName");
                        String email = (String) newContact.get("Email");
                        String role = (String) newContact.get("Title");
                        String phone = (String) newContact.get("Phone");
                        String accountId = (String) newContact.get("AccountId");

                        ContactsDto contactsDto = new ContactsDto();
                        if (accountId != null) {
                            Account account = this.accountRepository.findBySalesforceAccountId(accountId);
                            if (account != null) {
                                contactsDto.setCompanyName(account.getAccountName());
                                contactsDto.setSalesforceAccountId(accountId);
                                contactsDto.setAccountId(account.getId());
                            }
                        }

                        contactsDto.setFirstName(firstName);
                        contactsDto.setLastName(lastName);
                        contactsDto.setEmailAddress(email);
                        contactsDto.setTitle(role);
                        contactsDto.setCreatedBy(userId);
                        contactsDto.setRole(role);
                        contactsDto.setPhone(phone);
                        contactsDto.setSalesforceContactId(id);

                        Contacts contact = this.contactsRepository.findBySalesforceContactId(id);
                        if (contact == null) {
                            this.contactsService.createContact(contactsDto, false);
                        } else {
                            this.contactsService.updateContact(contact.getId(), contactsDto, false);
                        }
                    }
                }
            }
            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
            syncRecordsQueueDto.setSubject("Contact");
            syncRecordsQueueDto.setOperationType("GET");
            syncRecordsQueueDto.setSyncType("PULL");
            syncRecordsQueueDto.setCreatedBy(userId);
            syncRecordsQueueDto.setDeleted(true);
            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            response.put("message", "Contacts synced successfully");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContactIdIntoOpp(String accessToken, String instanceUrl, Integer userId) {
        try {
            List<OpportunityContact> opportunityContactList = this.opportunityContactRepository.findAll();
            if (!opportunityContactList.isEmpty()) {
                for (OpportunityContact opportunityContact : opportunityContactList) {
                    if (opportunityContact.getContacts() == null) {
                        Contacts contacts = this.contactsRepository.findBySalesforceContactId(opportunityContact.getSalesforceContactId());
                        if (contacts != null) {
                            opportunityContact.setContacts(contacts);
                            this.opportunityContactRepository.save(opportunityContact);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncCompetitor(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = this.salesforceCompetitorService.getAllOpportunityCompetitors(accessToken, instanceUrl);
            if (result.get("error") != null) {
                saveErrorQueue(result, "GET", userId, "Competitor");
                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response;
            } else {
                List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                if (records != null && !records.isEmpty()) {
                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");
                        String opportunityId = Objects.toString(record.get("OpportunityId"));
                        Opportunities opportunities = this.opportunitiesRepository.findBySalesforceOpportunityId(opportunityId);

                        ITLandscapeDto itLandscapeDto = new ITLandscapeDto();
                        itLandscapeDto.setSalesforceCompetitorId(id);
                        itLandscapeDto.setCustomerId(userId);
                        itLandscapeDto.setSalesforceOpportunityId((opportunityId != "" || opportunityId != null) ? opportunityId : null);
                        itLandscapeDto.setOpportunityId(opportunities != null ? opportunities.getId() : null);

                        ITLandscape existing = this.itLandscapeRepository.findBySalesforceCompetitorId(id);
                        if (existing == null) {
                            this.itLandscapeService.createITLandscape(itLandscapeDto, false);
                        } else {
                            this.itLandscapeService.updateITLandscape(existing.getId(), itLandscapeDto, false);
                        }
                    }
                } else {
                    response.put("error", "Invalid response format: 'records' is not a List");
                    throw new RuntimeException("Invalid response format: 'records' is not a List");
                }
            }

            SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
            syncRecordsQueueDto.setSubject("Competitor");
            syncRecordsQueueDto.setOperationType("GET");
            syncRecordsQueueDto.setSyncType("PULL");
            syncRecordsQueueDto.setCreatedBy(userId);
            syncRecordsQueueDto.setDeleted(true);
            this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);

            response.put("message", "Competitor synced successfully");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncStages(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = this.salesforceOpportunityStageService.getAllOpportunityStages(accessToken, instanceUrl);
            if (result.get("error") != null) {
                saveErrorQueue(result, "GET", userId, "Competitor");
                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response;
            } else {
                List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                if (records != null && !records.isEmpty()) {
                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");
                        String name = (String) record.get("ApiName");
                        String description = (String) record.get("Description");

                        SalesStagesDto salesStagesDto = new SalesStagesDto();
                        salesStagesDto.setShortName(name);
                        salesStagesDto.setDescription(description);
                        salesStagesDto.setSalesforceStageId(id);
                        salesStagesDto.setCrmId(1);
                        SalesStages existing = this.salesStagesRepository.findBySalesforceStageId(id);
                        if (existing == null) {
                            this.salesStageService.createSalesStage(salesStagesDto);
                        } else {
                            this.salesStageService.updateSalesStage(existing.getId(), salesStagesDto);
                        }
                    }
                } else {
                    response.put("error", "Invalid response format: 'records' is not a List");
                    throw new RuntimeException("Invalid response format: 'records' is not a List");
                }
            }
            response.put("message", "Stages synced successfully");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncProducts(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = this.salesforceProductsService.getAllProducts(accessToken, instanceUrl);
            if (result.get("error") != null) {
                saveErrorQueue(result, "GET", userId, "Products");
                response.put("success", false);
                response.put("error", result.get("error"));
                response.put("status", result.get("status"));
                response.put("message", result.get("message"));
                return response;
            } else {
                List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("products");
                if (records != null && !records.isEmpty()) {
                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");
                        String name = (String) record.get("Name");
                        String productCode = (String) record.get("ProductCode");
                        Object unitPriceObj = record.get("UnitPrice");

                        // ✅ Safely convert UnitPrice to int
                        float price = 0;
                        if (unitPriceObj instanceof Number) {
                            price = ((Number) unitPriceObj).intValue(); // handles Double, Integer, Long, etc.
                        } else if (unitPriceObj != null) {
                            try {
                                price = (float) Double.parseDouble(unitPriceObj.toString());
                            } catch (NumberFormatException e) {
                                price = 0; // or handle error as needed
                            }
                        }

                        String description = (String) record.get("Description");
                        boolean isActive = (Boolean) record.get("IsActive");

                        ProductsDto productsDto = new ProductsDto();
                        productsDto.setName(name);
                        productsDto.setCode(productCode);
                        productsDto.setPrice(price);
                        productsDto.setDescription(description);
                        productsDto.setIsActive(isActive);
                        productsDto.setType("Product");
                        productsDto.setCreatedBy(userId);
                        productsDto.setSalesforceProductId(id);

                        ProductsDto existing = this.productService.getProductBySalesForceId(id);
                        if (existing == null) {
                            this.productService.createProduct(userId, productsDto);
                        } else {
                            this.productService.updateProduct(existing.getId(), productsDto);
                        }
                    }
                } else {
                    response.put("error", "Invalid response format: 'records' is not a List");
                    throw new RuntimeException("Invalid response format: 'records' is not a List");
                }
            }

            response.put("message", "Products synced successfully");
            response.put("status", "success");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunityProducts(String accessToken, String instanceUrl, Integer userId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        Map<String, Object> response = new HashMap<>();
        try {
            List<Opportunities> opportunities = this.opportunitiesRepository.findAllSalesforceOpportunity();
            if (!opportunities.isEmpty()) {
                for (Opportunities opportunities1 : opportunities) {
                    Map<String, Object> result = this.salesforceOpportunityProductService.getAllOpportunityProductsByOppId(accessToken, instanceUrl, opportunities1.getSalesforceOpportunityId());
                    if (result.get("error") != null) {
                        // store in error queue and return error response
                        saveErrorQueue(result, "GET", userId, "OpportunitiesProducts");

                        response.put("success", false);
                        response.put("error", result.get("error"));
                        response.put("status", result.get("status"));
                        response.put("message", result.get("message"));
                        return response;
                    }
                    List<Map<String, Object>> records =
                            (List<Map<String, Object>>) result.get("records");

                    for (Map<String, Object> record : records) {
                        String id = (String) record.get("Id");
                        String productId = (String) record.get("Product2Id");
                        String serviceDate = (String) record.get("ServiceDate");

                        Number qtyNumber = (Number) record.get("Quantity");
                        Number listPriceNumber = (Number) record.get("UnitPrice");

                        OpportunityProductsDto opportunityProductsDto = new OpportunityProductsDto();
                        opportunityProductsDto.setOppId(opportunities1.getId());
                        opportunityProductsDto.setOpportunityProductId(id);
                        opportunityProductsDto.setIsDeleted(false);

                        if (qtyNumber != null) {
                            opportunityProductsDto.setQty(qtyNumber.intValue());
                        }

                        if (listPriceNumber != null) {
                            opportunityProductsDto.setPrice(listPriceNumber.floatValue());
                        }

                        opportunityProductsDto.setCreatedDate(serviceDate);

                        ProductsDto productsDto = this.productService.getProductBySalesForceId(productId);
                        if (productsDto != null) {
                            opportunityProductsDto.setProductId(productsDto.getId());
                            opportunityProductsDto.setName(productsDto.getName());
                        }

                        OpportunityProducts opportunityProducts =
                                this.opportunityProductsRepository.getBySalesForceOpportunityProductId(id);

                        if (opportunityProducts != null) {
                            this.opportunityProductsService.updateOppProducts(opportunityProducts.getId(), opportunityProductsDto, false, userId);
                        } else {
                            this.opportunityProductsService.createOppProducts(opportunityProductsDto, false, userId);
                        }
                    }

                }

                SyncRecordsQueueDto opportunitiesQueue = new SyncRecordsQueueDto();
                opportunitiesQueue.setSubject("OpportunitiesProducts");
                opportunitiesQueue.setOperationType("GET");
                opportunitiesQueue.setSyncType("PULL");
                opportunitiesQueue.setCreatedBy(userId);
                opportunitiesQueue.setDeleted(true);
                this.syncRecordsQueueService.createSyncRecord(opportunitiesQueue);
            }
            response.put("message", "OpportunitiesProducts synced successfully");
            response.put("status", "success");
            response.put("success", true);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("status", 500);
            response.put("error", e.getClass().getSimpleName());
            response.put("message", e.getMessage());
            return response;
        }
    }

    private void saveErrorQueue(Map<String, Object> errorResponse, String operationType, Integer userId, String subject) {
        SyncRecordsQueueDto error = new SyncRecordsQueueDto();
        error.setSubject(subject);
        error.setOperationType(operationType);
        error.setSyncType("PULL");
        error.setCreatedBy(userId);

        String errorMessage = "Failed to sync " + subject + " in Salesforce";

        if (errorResponse != null) {
            Object errObj = errorResponse.get("errorMessage");
            if (errObj instanceof String jsonString) {
                try {
                    // Parse the JSON string
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<Map<String, Object>> errors = objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                    });

                    // Extract only 'message' field from each map
                    List<String> messages = errors.stream()
                            .map(errMap -> errMap.get("message"))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .toList();

                    if (!messages.isEmpty()) {
                        errorMessage = String.join("; ", messages);
                    }
                } catch (Exception e) {
                    // If parsing fails, use the original string
                    errorMessage = jsonString;
                }
            } else if (errObj instanceof List<?> errors && !errors.isEmpty()) {
                // Handle case where it's already a List
                List<String> messages = errors.stream()
                        .filter(Map.class::isInstance)
                        .map(Map.class::cast)
                        .map(errMap -> errMap.get("message"))
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .toList();

                if (!messages.isEmpty()) {
                    errorMessage = String.join("; ", messages);
                }
            } else if (errObj != null) {
                errorMessage = errObj.toString();
            }
        }
        error.setError(errorMessage);
        error.setDeleted(true);
        this.syncRecordsQueueService.createSyncRecord(error);
    }
}