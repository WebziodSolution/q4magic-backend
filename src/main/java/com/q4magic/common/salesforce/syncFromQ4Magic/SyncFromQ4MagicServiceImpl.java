package com.q4magic.common.salesforce.syncFromQ4Magic;

import com.q4magic.common.dto.ProductsDto;
import com.q4magic.common.dto.SyncRecordsQueueDto;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.salesforce.account.service.SalesforceAccountService;
import com.q4magic.common.salesforce.contacts.service.SalesforceContactsService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunitiesService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityPartnerService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityProductService;
import com.q4magic.products.service.ProductService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

@Service(value = "SyncFromQ4MagicService")
public class SyncFromQ4MagicServiceImpl implements SyncFromQ4MagicService {
    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private SalesforceAccountService salesforceAccountService;

    @Autowired
    private SalesforceContactsService salesforceContactsService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SalesforceOpportunitiesService salesforceOpportunitiesService;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private OpportunityPartnerDetailsRepository opportunityPartnerDetailsRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SalesforceOpportunityPartnerService salesforceOpportunityPartnerService;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private OpportunityProductsRepository opportunityProductsRepository;


    @Autowired
    private SalesforceOpportunityProductService salesforceOpportunityProductService;

    @Autowired
    private ProductService productService;

    @Override
    public Map<String, Object> syncAccounts(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }

            Map<String, Object> response = new HashMap<>();

            // ========================================== CREATE ACCOUNTS ==========================================
            List<SyncRecordsQueue> syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("Account", "CREATE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Account account = this.accountRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Account not found with ID: " + syncRecordsQueue.getSubjectId()));

                    if (account.getSalesforceAccountId() == null) {
                        String name = account.getAccountName();
                        String phone = account.getPhone();

                        Map<String, Object> record = new HashMap<>();
                        record.put("Name", name);
                        record.put("Phone", phone);

                        Map<String, Object> newAccount = this.salesforceAccountService.createAccount(record, accessToken, instanceUrl);

                        if (newAccount != null && !newAccount.isEmpty()
                                && !newAccount.containsKey("error") && !newAccount.containsKey("errorMessage")) {

                            Map<String, Object> responseBody = (Map<String, Object>) newAccount.get("account");
                            String salesforceId = (String) responseBody.get("id");
                            account.setSalesforceAccountId(salesforceId);
                            this.accountRepository.save(account);

                            SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository
                                    .findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                            if (syncRecordsQueue1 != null) {
                                syncRecordsQueue1.setDeleted(true);
                                this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            }

                            response.put("message", "Account created in Salesforce: " + name);
                        } else {
                            saveErrorQueue(newAccount, "CREATE", userId, "Account");
                            response.put("error", "Failed to create account in Salesforce for: " + name);
                            return response;
                        }
                    }
                }
            }

            // ========================================== UPDATE ACCOUNTS ==========================================
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("Account", "UPDATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Account account = this.accountRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Account not found with ID: " + syncRecordsQueue.getSubjectId()));

                    String name = account.getAccountName();
                    String phone = account.getPhone();
                    String salesforceId = account.getSalesforceAccountId();

                    Map<String, Object> record = new HashMap<>();
                    record.put("Name", name);
                    record.put("Phone", phone);

                    Map<String, Object> updatedAccount =
                            this.salesforceAccountService.updateAccount(salesforceId, record, accessToken, instanceUrl);

                    if (updatedAccount != null && !updatedAccount.isEmpty()
                            && !updatedAccount.containsKey("error") && !updatedAccount.containsKey("errorMessage")) {

                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository
                                .findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Account updated in Salesforce: " + name);
                    } else {
                        saveErrorQueue(updatedAccount, "UPDATE", userId, "Account");
                        response.put("error", "Failed to update account in Salesforce for: " + name);
                        return response;
                    }
                }
            }

            // ========================================== DELETE ACCOUNTS ==========================================
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("Account", "DELETE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Account account = this.accountRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Account not found with ID: " + syncRecordsQueue.getSubjectId()));

                    String salesforceId = account.getSalesforceAccountId();

                    Map<String, Object> deletedAccount =
                            this.salesforceAccountService.deleteAccount(salesforceId, accessToken, instanceUrl);

                    if (deletedAccount != null && !deletedAccount.isEmpty()
                            && !deletedAccount.containsKey("error") && !deletedAccount.containsKey("errorMessage")) {

                        this.accountRepository.delete(account);
                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository
                                .findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Account deleted in Salesforce: " + account.getAccountName());
                    } else {
                        saveErrorQueue(deletedAccount, "DELETE", userId, "Account");
                        response.put("error", "Failed to delete account in Salesforce for: " + account.getAccountName());
                        return response;
                    }
                }
            } else {
                response.put("message", "No accounts found to sync");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunity(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }

            Map<String, Object> response = new HashMap<>();

            // ========================================== CREATE ==========================================
            List<SyncRecordsQueue> syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("Opportunities", "CREATE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Opportunities opportunity = this.opportunitiesRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Opportunity not found with ID: " + syncRecordsQueue.getSubjectId()));

                    if (opportunity.getSalesforceOpportunityId() == null) {
                        Map<String, Object> opportunityData = new HashMap<>();
                        opportunityData.put("Name", opportunity.getOpportunity());
                        opportunityData.put("StageName", opportunity.getSalesStage());
                        opportunityData.put("CloseDate", opportunity.getCloseDate());
                        opportunityData.put("Amount", opportunity.getDealAmount());
                        opportunityData.put("NextStep", opportunity.getNextSteps());
                        if (opportunity.getAccount() != null) {
                            opportunityData.put("AccountId", opportunity.getAccount().getSalesforceAccountId());
                        }

                        Map<String, Object> newOpportunity = this.salesforceOpportunitiesService.createOpportunity(accessToken, instanceUrl, opportunityData);

                        if (newOpportunity != null && !newOpportunity.isEmpty()
                                && !newOpportunity.containsKey("error")
                                && !newOpportunity.containsKey("errorMessage")) {

                            Map<String, Object> responseBody = (Map<String, Object>) newOpportunity.get("opportunity");
                            String salesforceId = (String) responseBody.get("id");
                            opportunity.setSalesforceOpportunityId(salesforceId);
                            this.opportunitiesRepository.save(opportunity);

                            SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                            if (syncRecordsQueue1 != null) {
                                syncRecordsQueue1.setDeleted(true);
                                this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            }

                            response.put("message", "Opportunity created in Salesforce");

                        } else {
                            saveErrorQueue(newOpportunity, "CREATE", userId, "Opportunities");
                            response.put("error", "Failed to create opportunity in Salesforce");
                            return response;
                        }
                    }
                }
            }

            // ========================================== UPDATE ==========================================
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("Opportunities", "UPDATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Opportunities opportunity = this.opportunitiesRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Opportunity not found with ID: " + syncRecordsQueue.getSubjectId()));

                    Map<String, Object> opportunityData = new HashMap<>();
                    opportunityData.put("Name", opportunity.getOpportunity());
                    opportunityData.put("StageName", opportunity.getSalesStage());
                    opportunityData.put("CloseDate", opportunity.getCloseDate());
                    opportunityData.put("Amount", opportunity.getDealAmount());
                    opportunityData.put("NextStep", opportunity.getNextSteps());
                    if (opportunity.getAccount() != null) {
                        opportunityData.put("AccountId", opportunity.getAccount().getSalesforceAccountId());
                    }

                    String salesforceId = opportunity.getSalesforceOpportunityId();
                    Map<String, Object> updatedOpportunity =
                            this.salesforceOpportunitiesService.updateOpportunity(accessToken, instanceUrl, salesforceId, opportunityData);

                    if (updatedOpportunity != null && !updatedOpportunity.isEmpty()
                            && !updatedOpportunity.containsKey("error")
                            && !updatedOpportunity.containsKey("errorMessage")) {

                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }
                        if (opportunity.getSalesStage().equals("Closed Lost") || opportunity.getStatus().equals("Lost")) {
                            this.opportunitiesRepository.delete(opportunity);
                        }
                        response.put("message", "Opportunity updated in Salesforce");

                    } else {
                        saveErrorQueue(updatedOpportunity, "UPDATE", userId, "Opportunities");
                        response.put("error", "Failed to update opportunity in Salesforce");
                        return response;
                    }
                }
            }

            // ========================================== DELETE ==========================================
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("Opportunities", "DELETE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Opportunities opportunity = this.opportunitiesRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Opportunity not found with ID: " + syncRecordsQueue.getSubjectId()));

                    String salesforceId = opportunity.getSalesforceOpportunityId();
                    Map<String, Object> deletedOpportunity =
                            this.salesforceOpportunitiesService.deleteOpportunity(accessToken, instanceUrl, salesforceId);

                    if (deletedOpportunity != null && !deletedOpportunity.isEmpty()
                            && !deletedOpportunity.containsKey("error")
                            && !deletedOpportunity.containsKey("errorMessage")) {

                        this.opportunitiesRepository.delete(opportunity);

                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Opportunity deleted in Salesforce");

                    } else {
                        saveErrorQueue(deletedOpportunity, "DELETE", userId, "Opportunities");
                        response.put("error", "Failed to delete opportunity in Salesforce");
                        return response;
                    }
                }
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncContacts(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }

            Map<String, Object> response = new HashMap<>();

            // ========================================== CREATE ==========================================
            List<SyncRecordsQueue> syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("Contact", "CREATE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Contacts contact = this.contactsRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + syncRecordsQueue.getSubjectId()));

                    if (contact.getSalesforceContactId() == null) {
                        Map<String, Object> contactData = new HashMap<>();
                        contactData.put("FirstName", contact.getFirstName());
                        contactData.put("LastName", contact.getLastName());
                        contactData.put("Email", contact.getEmailAddress());
                        contactData.put("Title", contact.getTitle());
                        contactData.put("Phone", contact.getPhone());
                        if (contact.getAccount() != null) {
                            contactData.put("AccountId", contact.getAccount().getSalesforceAccountId());
                        }

//                    contactData.put("AccountId", contact.getAccount().getSalesforceAccountId());

                        Map<String, Object> newContact =
                                this.salesforceContactsService.createContact(contactData, accessToken, instanceUrl);

                        if (newContact != null && !newContact.isEmpty()
                                && !newContact.containsKey("error")
                                && !newContact.containsKey("errorMessage")) {

                            Map<String, Object> responseBody = (Map<String, Object>) newContact.get("contact");
                            String salesforceId = (String) responseBody.get("id");
                            contact.setSalesforceContactId(salesforceId);
                            this.contactsRepository.save(contact);

                            SyncRecordsQueue syncRecordsQueue1 =
                                    this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                            if (syncRecordsQueue1 != null) {
                                syncRecordsQueue1.setDeleted(true);
                                this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            }

                            response.put("message", "Contact created in Salesforce");

                        } else {
                            saveErrorQueue(newContact, "CREATE", userId, "Contact");
                            response.put("error", "Failed to create contact in Salesforce");
                            return response;
                        }
                    }
                }
            }

            // ========================================== UPDATE ==========================================
            syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("Contact", "UPDATE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Contacts contact = this.contactsRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + syncRecordsQueue.getSubjectId()));

                    Map<String, Object> contactData = new HashMap<>();
                    contactData.put("FirstName", contact.getFirstName());
                    contactData.put("LastName", contact.getLastName());
                    contactData.put("Email", contact.getEmailAddress());
                    contactData.put("Title", contact.getTitle());
//                contactData.put("AccountId", contact.getAccount().getSalesforceAccountId());

                    String salesforceId = contact.getSalesforceContactId();
                    Map<String, Object> updatedContact =
                            this.salesforceContactsService.updateContact(salesforceId, contactData, accessToken, instanceUrl);

                    if (updatedContact != null && !updatedContact.isEmpty()
                            && !updatedContact.containsKey("error")
                            && !updatedContact.containsKey("errorMessage")) {

                        SyncRecordsQueue syncRecordsQueue1 =
                                this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Contact updated in Salesforce");

                    } else {
                        saveErrorQueue(updatedContact, "UPDATE", userId, "Contact");
                        response.put("error", "Failed to update contact in Salesforce");
                        return response;
                    }
                }
            }

            // ========================================== DELETE ==========================================
            syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("Contact", "DELETE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    Contacts contact = this.contactsRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Contact not found with ID: " + syncRecordsQueue.getSubjectId()));

                    String salesforceId = contact.getSalesforceContactId();
                    Map<String, Object> deletedContact =
                            this.salesforceContactsService.deleteContact(salesforceId, accessToken, instanceUrl);

                    if (deletedContact != null && !deletedContact.isEmpty()
                            && !deletedContact.containsKey("error")
                            && !deletedContact.containsKey("errorMessage")) {

                        this.contactsRepository.delete(contact);

                        SyncRecordsQueue syncRecordsQueue1 =
                                this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Contact deleted in Salesforce");

                    } else {
                        saveErrorQueue(deletedContact, "DELETE", userId, "Contact");
                        response.put("error", "Failed to delete contact in Salesforce");
                        return response;
                    }
                }
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunityPartner(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }
            Map<String, Object> response = new HashMap<>();
//            ========================================== Sync OpportunityPartner from Q4Magic to Salesforce ==========================================
            List<SyncRecordsQueue> syncRecordsQueues = new ArrayList<>();
//            ---------------------- Create OpportunityPartner --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesPartner", "CREATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("OpportunityPartner not found with ID: " + syncRecordsQueue.getSubjectId()));
                    if (opportunityPartnerDetails.getSalesforceOpportunityPartnerId() == null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("Role", opportunityPartnerDetails.getRole());
                        data.put("AccountToId", opportunityPartnerDetails.getAccountToId());
                        data.put("OpportunityId", opportunityPartnerDetails.getOpportunities().getSalesforceOpportunityId());
                        data.put("IsPrimary", opportunityPartnerDetails.getIsPrimary());
                        Map<String, Object> newOppPartner = this.salesforceOpportunityPartnerService.createOpportunityPartner(accessToken, instanceUrl, data);
                        if (newOppPartner != null && !newOppPartner.isEmpty() && !newOppPartner.containsKey("error") && !newOppPartner.containsKey("errorMessage")) {
                            Map<String, Object> responseBody = (Map<String, Object>) newOppPartner.get("data");
                            String salesforceId = (String) responseBody.get("id");
                            opportunityPartnerDetails.setSalesforceOpportunityPartnerId(salesforceId);
                            this.opportunityPartnerDetailsRepository.save(opportunityPartnerDetails);
                            SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);

                            if (syncRecordsQueue1 != null) {
                                syncRecordsQueue1.setDeleted(true);
                                this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            }
                            response.put("message", "OpportunityPartner created in Salesforce");
                        } else {
                            saveErrorQueue(newOppPartner, "CREATE", userId, "OpportunitiesPartner");
                            response.put("error", "Failed to create opportunityPartner in Salesforce");
                            return response;
                        }
                    }
                }
            }
//            ---------------------- Update OpportunityPartner --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesPartner", "UPDATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("OpportunityPartner not found with ID: " + syncRecordsQueue.getSubjectId()));
                    Map<String, Object> data = new HashMap<>();
                    data.put("Role", opportunityPartnerDetails.getRole());
                    data.put("AccountToId", opportunityPartnerDetails.getAccountToId());
                    data.put("OpportunityId", opportunityPartnerDetails.getOpportunities().getSalesforceOpportunityId());
                    data.put("IsPrimary", opportunityPartnerDetails.getIsPrimary());

                    String salesforceId = opportunityPartnerDetails.getSalesforceOpportunityPartnerId();
                    Map<String, Object> updatedOppPartner = this.salesforceOpportunityPartnerService.updateOpportunityPartner(accessToken, instanceUrl, salesforceId, data);
                    if (updatedOppPartner != null && !updatedOppPartner.isEmpty() && !updatedOppPartner.containsKey("error") && !updatedOppPartner.containsKey("errorMessage")) {
                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }
                        response.put("message", "OpportunityPartner updated in Salesforce");
                    } else {
                        saveErrorQueue(updatedOppPartner, "UPDATE", userId, "OpportunitiesPartner");
                        response.put("error", "Failed to update opportunityPartner in Salesforce");
                        return response;
                    }
                }
            }
//            ---------------------- Delete OpportunityPartner --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesPartner", "DELETE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityPartnerDetails opportunityPartnerDetails = this.opportunityPartnerDetailsRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("OpportunityPartner not found with ID: " + syncRecordsQueue.getSubjectId()));
                    String salesforceId = opportunityPartnerDetails.getSalesforceOpportunityPartnerId();
                    Map<String, Object> deletedOppPartner = this.salesforceOpportunityPartnerService.deleteOpportunityPartner(accessToken, instanceUrl, salesforceId);
                    if (deletedOppPartner != null && !deletedOppPartner.isEmpty() && !deletedOppPartner.containsKey("error") && !deletedOppPartner.containsKey("errorMessage")) {
                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            this.opportunityPartnerDetailsRepository.delete(opportunityPartnerDetails);
                        }
                        response.put("message", "OpportunityPartner deleted in Salesforce");
                    } else {
                        saveErrorQueue(deletedOppPartner, "DELETE", userId, "OpportunitiesPartner");
                        response.put("error", "Failed to delete OpportunityPartner in Salesforce");
                        return response;
                    }
                }
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunityContacts(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }
            Map<String, Object> response = new HashMap<>();
//            ========================================== Sync OpportunitiesContacts from Q4Magic to Salesforce ==========================================
            List<SyncRecordsQueue> syncRecordsQueues = new ArrayList<>();
//            ---------------------- Create OpportunitiesContacts --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesContacts", "CREATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityContact opportunityContact = this.opportunityContactRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("OpportunitiesContacts not found with ID: " + syncRecordsQueue.getSubjectId()));
                    if (opportunityContact.getSalesforceOpportunityContactId() == null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("Role", opportunityContact.getRole());
                        data.put("OpportunityId", opportunityContact.getOpportunities().getSalesforceOpportunityId());
                        data.put("ContactId", opportunityContact.getSalesforceContactId());
                        data.put("IsPrimary", opportunityContact.getIsKey());

                        Map<String, Object> newOppContact = this.salesforceOpportunitiesService.createOpportunityContact(instanceUrl, accessToken, data);
                        if (newOppContact != null && !newOppContact.isEmpty() && !newOppContact.containsKey("error") && !newOppContact.containsKey("errorMessage")) {
                            Map<String, Object> responseBody = (Map<String, Object>) newOppContact.get("result");
                            String salesforceId = (String) responseBody.get("id");
                            opportunityContact.setSalesforceOpportunityContactId(salesforceId);
                            this.opportunityContactRepository.save(opportunityContact);
                            SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                            if (syncRecordsQueue1 != null) {
                                syncRecordsQueue1.setDeleted(true);
                                this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            }

                            response.put("message", "OpportunitiesContacts created in Salesforce");
                        } else {
                            saveErrorQueue(newOppContact, "CREATE", userId, "OpportunitiesContacts");
                            response.put("error", "Failed to create OpportunitiesContacts in Salesforce");
                            return response;
                        }
                    }
                }
            }
//            ---------------------- Update OpportunitiesContacts --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesContacts", "UPDATE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityContact opportunityContact = this.opportunityContactRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("opportunityContact not found with ID: " + syncRecordsQueue.getSubjectId()));
                    Map<String, Object> data = new HashMap<>();
                    data.put("Role", opportunityContact.getRole());
                    data.put("IsPrimary", opportunityContact.getIsKey());

                    String salesforceOppId = opportunityContact.getSalesforceOpportunityContactId();

                    Map<String, Object> updatedOppPartner = this.salesforceOpportunitiesService.updateOpportunityContact(instanceUrl, accessToken, salesforceOppId, data);
                    if (updatedOppPartner != null && !updatedOppPartner.isEmpty() && !updatedOppPartner.containsKey("error") && !updatedOppPartner.containsKey("errorMessage")) {
                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }
                        response.put("message", "OpportunitiesContacts updated in Salesforce");
                    } else {
                        saveErrorQueue(updatedOppPartner, "UPDATE", userId, "OpportunitiesContacts");
                        response.put("error", "Failed to update OpportunitiesContacts in Salesforce");
                        return response;
                    }
                }
            }
//            ---------------------- Delete OpportunitiesContacts --------------------
            syncRecordsQueues = this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesContacts", "DELETE", "PUSH", userId);
            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityContact opportunityContact = this.opportunityContactRepository.findById(syncRecordsQueue.getSubjectId()).orElseThrow(() -> new RuntimeException("opportunityContact not found with ID: " + syncRecordsQueue.getSubjectId()));
                    String salesforceOppId = opportunityContact.getSalesforceOpportunityContactId();
                    Map<String, Object> deletedOppContact = this.salesforceOpportunitiesService.deleteOpportunityContact(instanceUrl, accessToken, salesforceOppId);
                    if (deletedOppContact != null && !deletedOppContact.isEmpty() && !deletedOppContact.containsKey("error") && !deletedOppContact.containsKey("errorMessage")) {
                        SyncRecordsQueue syncRecordsQueue1 = this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                            this.opportunityContactRepository.delete(opportunityContact);
                        }
                        response.put("message", "deletedOppContact deleted in Salesforce");
                    } else {
                        saveErrorQueue(deletedOppContact, "DELETE", userId, "OpportunitiesContacts");
                        response.put("error", "Failed to delete deletedOppContact in Salesforce");
                        return response;
                    }
                }
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> syncOpportunityProducts(String accessToken, String instanceUrl, Integer userId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                throw new IllegalArgumentException("Access token cannot be null or empty");
            }

            Map<String, Object> response = new HashMap<>();

            // ========================================== CREATE ==========================================
            List<SyncRecordsQueue> syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesProducts", "CREATE", "PUSH", userId);

            for (SyncRecordsQueue queue : syncRecordsQueues) {
                // 1️⃣ Load local Opportunity Product
                OpportunityProducts opportunityProducts =
                        this.opportunityProductsRepository.findById(queue.getSubjectId()).orElse(null);

                if (opportunityProducts == null) {
                    // mark queue as error or delete – record is gone
                    queue.setDeleted(true);
                    this.syncRecordsQueueRepository.save(queue);
                    continue;
                }

                // 2️⃣ Load local Opportunity + Product to get Salesforce IDs
                Opportunities opp = this.opportunitiesRepository.findById(opportunityProducts.getOpportunities().getId()).orElse(null);
                ProductsDto productDto = this.productService.getProduct(opportunityProducts.getProducts().getId());

                if (opp == null ||
                        opp.getSalesforceOpportunityId() == null ||
                        productDto == null ||
                        productDto.getSalesforceProductId() == null) {

                    // You can send this to error queue; for now just skip
                    saveErrorQueue(null, "CREATE", userId, "OpportunitiesProducts");
                    continue;
                }

                String opportunitySfId = opp.getSalesforceOpportunityId();
                String productSfId = productDto.getSalesforceProductId();

                // 3️⃣ Ensure Opportunity has Pricebook2Id & get PricebookEntryId
                String pricebookEntryId =
                        this.salesforceOpportunityProductService.ensureOpportunityPricebookAndGetEntryId(
                                accessToken,
                                instanceUrl,
                                opportunitySfId,
                                productSfId
                        );

                Map<String, Object> productData = new HashMap<>();
                productData.put("OpportunityId", opportunitySfId);
                productData.put("PricebookEntryId", pricebookEntryId);
                productData.put("Quantity", opportunityProducts.getQty());
                productData.put("UnitPrice", opportunityProducts.getPrice());
                // ❌ DO NOT send ProductCode / ListPrice / Name / Product2Id here

                Map<String, Object> result =
                        this.salesforceOpportunityProductService.createOpportunityProduct(accessToken, instanceUrl, productData);

                Boolean success = (Boolean) result.get("success");
                if (Boolean.TRUE.equals(success)) {
                    Map<String, Object> resultBody = (Map<String, Object>) result.get("result");
                    String newSfId = (String) resultBody.get("id");

                    // 6️⃣ Store Salesforce OpportunityLineItem Id back into DB
                    opportunityProducts.setOpportunityProductId(newSfId);
                    this.opportunityProductsRepository.save(opportunityProducts);

                    // 7️⃣ Mark queue as processed
                    queue.setDeleted(true);
                    this.syncRecordsQueueRepository.save(queue);
                } else {
                    // Store error in some error queue if you want
                    saveErrorQueue(result, "CREATE", userId, "OpportunitiesProducts");
                }
            }


            // ========================================== UPDATE ==========================================
            syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesProducts", "UPDATE", "PUSH", userId);

            for (SyncRecordsQueue queue : syncRecordsQueues) {
                OpportunityProducts opportunityProducts =
                        this.opportunityProductsRepository.findById(queue.getSubjectId()).orElse(null);

                if (opportunityProducts == null ||
                        opportunityProducts.getOpportunityProductId() == null) {
                    queue.setDeleted(true);
                    this.syncRecordsQueueRepository.save(queue);
                    continue;
                }


                Map<String, Object> productData = new HashMap<>();
                productData.put("Quantity", opportunityProducts.getQty());
                productData.put("UnitPrice", opportunityProducts.getPrice());

                Map<String, Object> result =
                        this.salesforceOpportunityProductService.updateOpportunityProduct(
                                accessToken,
                                instanceUrl,
                                opportunityProducts.getOpportunityProductId(),   // Salesforce Id of OLI
                                productData
                        );

                Boolean success = (Boolean) result.get("success");
                if (Boolean.TRUE.equals(success)) {
                    queue.setDeleted(true);
                    this.syncRecordsQueueRepository.save(queue);
                } else {
                    saveErrorQueue(result, "UPDATE", userId, "OpportunitiesProducts");
                }
            }


            // ========================================== DELETE ==========================================
            syncRecordsQueues =
                    this.syncRecordsQueueRepository.findBySubjectAndType("OpportunitiesProducts", "DELETE", "PUSH", userId);

            if (!syncRecordsQueues.isEmpty()) {
                for (SyncRecordsQueue syncRecordsQueue : syncRecordsQueues) {
                    OpportunityProducts opportunityProducts = this.opportunityProductsRepository.findById(syncRecordsQueue.getSubjectId())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + syncRecordsQueue.getSubjectId()));

                    String salesforceId = opportunityProducts.getOpportunityProductId();
                    Map<String, Object> deletedOppProduct =
                            this.salesforceOpportunityProductService.deleteOpportunityProduct(accessToken, instanceUrl, salesforceId);

                    if (deletedOppProduct != null && !deletedOppProduct.isEmpty()
                            && !deletedOppProduct.containsKey("error")
                            && !deletedOppProduct.containsKey("errorMessage")) {

                        this.opportunityProductsRepository.delete(opportunityProducts);

                        SyncRecordsQueue syncRecordsQueue1 =
                                this.syncRecordsQueueRepository.findBySubjectId(syncRecordsQueue.getSubjectId(), userId);
                        if (syncRecordsQueue1 != null) {
                            syncRecordsQueue1.setDeleted(true);
                            this.syncRecordsQueueRepository.save(syncRecordsQueue1);
                        }

                        response.put("message", "Opp-product deleted in Salesforce");

                    } else {
                        saveErrorQueue(deletedOppProduct, "DELETE", userId, "OpportunitiesProducts");
                        response.put("error", "Failed to delete opp-product in Salesforce");
                        return response;
                    }
                }
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void saveErrorQueue(Map<String, Object> errorResponse, String operationType, Integer userId, String subject) {
        SyncRecordsQueueDto error = new SyncRecordsQueueDto();
        error.setSubject(subject);
        error.setOperationType(operationType);
        error.setSyncType("PUSH");
        error.setCreatedBy(userId);

        String errorMessage = "Failed to sync " + subject + " in Salesforce";

        if (errorResponse != null) {
            Object errObj = errorResponse.get("errorMessage");
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                if (errObj instanceof String jsonString) {
                    // Try to parse JSON string if possible
                    if (jsonString.trim().startsWith("[")) {
                        List<Map<String, Object>> errors = objectMapper.readValue(jsonString, new TypeReference<>() {
                        });
                        errorMessage = extractMessages(errors, errorMessage);
                    } else {
                        errorMessage = jsonString;
                    }
                } else if (errObj instanceof List<?> errorList) {
                    // Already a List of maps
                    List<Map<String, Object>> errors = errorList.stream()
                            .filter(Map.class::isInstance)
                            .map(e -> (Map<String, Object>) e)
                            .toList();
                    errorMessage = extractMessages(errors, errorMessage);
                } else {
                    // Anything else → just stringify
                    errorMessage = String.valueOf(errObj);
                }
            } catch (Exception e) {
                System.err.println("Error parsing Salesforce error message: " + e.getMessage());
            }
        }

        error.setError(errorMessage);
        error.setDeleted(true);
        this.syncRecordsQueueService.createSyncRecord(error);
    }

    private String extractMessages(List<Map<String, Object>> errors, String defaultMsg) {
        List<String> messages = errors.stream()
                .map(m -> m.get("message"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        return !messages.isEmpty() ? String.join("; ", messages) : defaultMsg;
    }

}