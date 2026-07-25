package com.q4magic.contacts.serviceImpl;

import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.contacts.service.ContactsService;
import com.q4magic.opportunityContactNotes.service.OpportunityContactNotesService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import com.q4magic.tempMail.service.TempMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "contactsService")
public class ContactsServiceImpl implements ContactsService {

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private TempMailService tempMailService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private OpportunityContactNotesService opportunityContactNotesService;

    @Override
    public List<ContactsDto> searchContacts(Integer userId, String value) {
        try {
            List<Contacts> contactsList = this.contactsRepository.searchByFirstOrLastName(userId, value);
            List<ContactsDto> contactsDtoList = new ArrayList<>();

            if (!contactsList.isEmpty()) {
                for (Contacts contact : contactsList) {
                    contactsDtoList.add(this.getContactById(contact.getId()));
                }
            }
            return contactsDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ContactsDto> getAllContacts(Integer userId, String fetchType) {
        try {
            Customers me = customersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Collect all owner IDs we should fetch contacts for
            List<Integer> ownerIds = new ArrayList<>();
            ownerIds.add(userId);

            if (me.getRole() != null && "SALES REPRESENTIVE".equals(me.getRole().getRole()) || me.getRole() != null && "SALES MANAGER".equals(me.getRole().getRole())) {
                List<Customers> subUsers = customersRepository.getAllSubUsers(userId);
                if (subUsers != null && !subUsers.isEmpty()) {
                    for (Customers sub : subUsers) {
                        ownerIds.add(sub.getId());
                    }
                }
            } else {
                // Also include parent customer (if present)
                if (me.getCustomers() != null) {
                    ownerIds.add(me.getCustomers().getId());
                }
            }

            // Ask Spring Data for DESC by id for each owner, then merge de-duped
            Sort sortDescById = Sort.by(Sort.Direction.DESC, "id");
            Map<Integer, Contacts> merged = new LinkedHashMap<>();
            for (Integer ownerId : ownerIds) {
                List<Contacts> list = contactsRepository
                        .findByIsDeletedFalseAndCustomersId(ownerId, sortDescById);
                for (Contacts c : list) {
                    merged.putIfAbsent(c.getId(), c);
                }
            }

            // Ensure global DESC across all owners
            List<Contacts> ordered = merged.values().stream()
                    .sorted(Comparator.comparing(Contacts::getId).reversed())
                    .collect(Collectors.toList());

            // Map to DTOs
            List<ContactsDto> contactsDtoList = new ArrayList<>(ordered.size());
            for (Contacts contact : ordered) {
                contactsDtoList.add(this.getContactById(contact.getId()));
            }
            return contactsDtoList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ContactsDto getContactById(Integer id) {
        try {
            Contacts contact = this.contactsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
            ContactsDto contactDto = new ContactsDto();
            contactDto.setCreatedBy(contact.getCustomers().getId());
            if (contact.getAccount() != null) {
                contactDto.setAccountId(contact.getAccount().getId());
                contactDto.setCompanyName(contact.getAccount().getAccountName());
            }
            if (contact.getContacts() != null) {
                contactDto.setReportContactId(contact.getContacts().getId());
            }
            BeanUtils.copyProperties(contact, contactDto);
            return contactDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ContactsDto createContact(ContactsDto contactDto, Boolean syncToSalesforce) {
        try {
            Contacts contact = new Contacts();
            if (contactDto.getAccountId() != null) {
                Account account = this.accountRepository.findById(contactDto.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Account not found with id: " + contactDto.getAccountId()));
                contact.setAccount(account);
                contact.setSalesforceAccountId(contactDto.getSalesforceAccountId());
                contact.setCompanyName(account.getAccountName());
            }
            Customers customer = this.customersRepository.findById(contactDto.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + contactDto.getCreatedBy()));
            if (customer.getCustomers() != null) {
                this.customersRepository.findById(customer.getCustomers().getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
            if (contactDto.getReportContactId() != null) {
                Contacts reportContact = this.contactsRepository.findById(contactDto.getReportContactId())
                        .orElseThrow(() -> new RuntimeException("Contact not found with id: " + contactDto.getCreatedBy()));
                contact.setContacts(reportContact);
            }
            contact.setCustomers(customer);
            contact.setIsDeleted(false);
            if (contactDto.getLastName() == null || contactDto.getLastName().equals("")) {
                contact.setLastName("");
            } else {
                contact.setLastName(contactDto.getLastName());
            }
            contact.setCreatedAt(new Date());
            contact.setFromMailScraping(false);
            BeanUtils.copyProperties(contactDto, contact, "id", "isDeleted", "opportunities", "lastName", "companyName");
            this.contactsRepository.save(contact);
            if (syncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(contact.getId());
                syncRecordsQueueDto.setSubject("Contact");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(contactDto.getCreatedBy());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            contactDto.setId(contact.getId());
            return contactDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ContactsDto updateContact(Integer id, ContactsDto contactDto, Boolean syncToSalesforce) {
        try {
            Contacts contact = this.contactsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
            contact.setTitle(contactDto.getTitle());
            contact.setRole(contactDto.getRole());

            if (contactDto.getAccountId() != null) {
                Account account = this.accountRepository.findById(contactDto.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Account not found with id: " + contactDto.getAccountId()));
                contact.setAccount(account);
                contact.setSalesforceAccountId(contactDto.getSalesforceAccountId());
                contact.setCompanyName(account.getAccountName());
            }
            if (contactDto.getReportContactId() != null) {
                Contacts reportContact = this.contactsRepository.findById(contactDto.getReportContactId())
                        .orElseThrow(() -> new RuntimeException("Contact not found with id: " + contactDto.getCreatedBy()));
                contact.setContacts(reportContact);
            }
            contact.setIsDeleted(false);
            if (contactDto.getLastName() == null || contactDto.getLastName().equals("")) {
                contact.setLastName("");
            } else {
                contact.setLastName(contactDto.getLastName());
            }
            BeanUtils.copyProperties(contactDto, contact, "id", "isDeleted", "opportunities", "lastName", "companyName");
            this.contactsRepository.save(contact);
            if (syncToSalesforce && contact.getSalesforceContactId() != null) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(id);
                syncRecordsQueueDto.setSubject("Contact");
                syncRecordsQueueDto.setOperationType("UPDATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(contact.getCustomers().getId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
            return contactDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteContact(Integer id, Boolean syncToSalesforce) {
        try {
            Contacts contact = this.contactsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));

            if (syncToSalesforce && contact.getSalesforceContactId() != null) {
                contact.setIsDeleted(true);
                this.contactsRepository.save(contact);
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(id);
                syncRecordsQueueDto.setSubject("Contact");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(contact.getCustomers().getId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            } else {
                SyncRecordsQueueDto existingSyncRecord = this.syncRecordsQueueService.findBySubjectId(id, contact.getCustomers().getId());
                if (existingSyncRecord != null) {
                    this.syncRecordsQueueService.deleteSyncRecord(existingSyncRecord.getId());
                }
                this.contactsRepository.delete(contact);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addContacts(Integer userId, List<Integer> tempMailIds) {
        try {
            for (Integer id : tempMailIds) {
                TempMailDto tempMailDto = this.tempMailService.getTempMailById(id);
                Contacts contact = new Contacts();
                Customers customer = this.customersRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                contact.setIsDeleted(false);
                contact.setFirstName(tempMailDto.getFirstName());
                if (tempMailDto.getLastName() == null || tempMailDto.getLastName().equals("")) {
                    contact.setLastName("");
                } else {
                    contact.setLastName(tempMailDto.getLastName());
                }
                contact.setCustomers(customer);
                contact.setEmailAddress(tempMailDto.getEmail() != null ? tempMailDto.getEmail() : null);
                contact.setTitle(tempMailDto.getJobTitle());
                contact.setPhone(tempMailDto.getPhone());
                contact.setCreatedAt(new Date());
                contact.setFromMailScraping(true);
                this.contactsRepository.save(contact);

                this.tempMailService.deleteTempMail(id);

                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(contact.getId());
                syncRecordsQueueDto.setSubject("Contact");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(userId);
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addMultipleContacts(Integer userId, List<Map<String, Object>> contactList) {
        try {
            for (Map<String, Object> map : contactList) {
                String firstName = (String) map.get("firstName");
                String lastName = (String) map.get("lastName");
                Integer oppId = (Integer) map.get("oppId");
                Integer contactId = (Integer) map.get("contactId");
                String role = (String) map.get("role");
                String title = (String) map.get("title");
                List<Map<String, Object>> opportunityContactNotesList = (List<Map<String, Object>>) map.get("opportunityContactNotesList");

                if (contactId != null) {
                    Contacts contacts = this.contactsRepository.findById(contactId).orElseThrow(() -> new RuntimeException("Contact not found with id: " + contactId));
                    Opportunities opportunities = opportunitiesRepository
                            .findById(oppId)
                            .orElseThrow(() -> new RuntimeException("Opp not found"));

                    OpportunityContact opportunityContact = new OpportunityContact();
                    opportunityContact.setContacts(contacts);
                    opportunityContact.setOpportunities(opportunities);
                    opportunityContact.setRole(role);
                    opportunityContact.setTitle(title);
                    opportunityContact.setIsDeleted(false);
                    Boolean isKey = (Boolean) map.get("isKeyContact");
                    opportunityContact.setIsKey(isKey != null && isKey);

                    opportunityContactRepository.save(opportunityContact);
                    List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                    if (!opportunityContactNotesList.isEmpty()){
                        for (Map<String, Object> obj:opportunityContactNotesList){
                            OpportunityContactNotesDto opportunityContactNotesDto = new OpportunityContactNotesDto();
                            opportunityContactNotesDto.setOpportunityContactId(opportunityContact.getId());
                            opportunityContactNotesDto.setType((String) obj.get("type"));
                            opportunityContactNotesDto.setNote((String) obj.get("note"));
                            opportunityContactNotesDtoList.add(opportunityContactNotesDto);
                        }
                        this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
                    }
                } else {
                    if (firstName == null && lastName == null) {
                        throw new RuntimeException("Name is required");
                    }
                    if (oppId == null) {
                        throw new RuntimeException("Opportunity id is required");
                    }

                    Contacts contact = new Contacts();
                    Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                    contact.setFirstName(firstName);
                    contact.setLastName(lastName);
                    contact.setTitle((String) map.get("title"));
                    contact.setRole((String) map.get("role"));
                    contact.setFromMailScraping(false);
                    contact.setCustomers(customers);
                    contact.setIsDeleted(false);
                    contactsRepository.save(contact);

                    Opportunities opportunities = opportunitiesRepository
                            .findById(oppId)
                            .orElseThrow(() -> new RuntimeException("Opp not found"));

                    OpportunityContact opportunityContact = new OpportunityContact();
                    opportunityContact.setContacts(contact);
                    opportunityContact.setOpportunities(opportunities);
                    opportunityContact.setRole((String) map.get("role"));
                    Boolean isKey = (Boolean) map.get("isKeyContact");
                    opportunityContact.setIsKey(isKey != null && isKey);
                    opportunityContact.setIsDeleted(false);
                    opportunityContactRepository.save(opportunityContact);

                    List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
                    if (!opportunityContactNotesList.isEmpty()){
                        for (Map<String, Object> obj:opportunityContactNotesList){
                            OpportunityContactNotesDto opportunityContactNotesDto = new OpportunityContactNotesDto();
                            opportunityContactNotesDto.setOpportunityContactId(opportunityContact.getId());
                            opportunityContactNotesDto.setType((String) obj.get("type"));
                            opportunityContactNotesDto.setNote((String) obj.get("note"));
                            opportunityContactNotesDtoList.add(opportunityContactNotesDto);
                        }
                        this.opportunityContactNotesService.createOrUpdateOppContactNotes(opportunityContactNotesDtoList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // ============================================================
    // ✅ UPDATED REPORT HIERARCHY LOGIC (matches your requirement)
    // ============================================================

    @Override
    public Map<String, Object> reportHierarch(Integer contactId) {
        try {
            Contacts start = contactsRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

            // Keep hierarchy inside the same owner/customer (adjust if you want cross-owner)
            Integer ownerId = (start.getCustomers() != null) ? start.getCustomers().getId() : null;

            // 1) Build upward chain: start -> manager -> ... -> top (cycle-safe)
            List<Contacts> upward = collectUpwardChain(start);

            // upward is [start, manager, grandManager, ... top]
            // root should be the TOP
            Collections.reverse(upward); // [top ... manager start]

            // 2) We want a path list of IDs: [topId, ..., startId]
            List<Integer> pathIds = upward.stream()
                    .map(Contacts::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (pathIds.isEmpty()) {
                // extremely defensive fallback
                return toMapLite(start, null);
            }

            // 3) Build a tree from the top, expanding siblings at each manager level.
            // Only expand children deeper for the node on the path; other siblings are leaf nodes.
            Set<Integer> buildVisited = new HashSet<>();
            ContactNodeDto root = buildHierarchyAlongPath(
                    pathIds.get(0),
                    ownerId,
                    pathIds,
                    1,
                    buildVisited
            );

            // 4) Serialize
            return toMap(root);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Collects chain upward from start until no manager or a cycle is detected.
     * Returns: [start, manager, grandManager, ... top]
     */
    private List<Contacts> collectUpwardChain(Contacts start) {
        List<Contacts> chain = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        Contacts cur = start;
        while (cur != null) {
            Integer id = cur.getId();
            if (id != null && !visited.add(id)) {
                // cycle detected -> stop
                break;
            }
            chain.add(cur);
            cur = cur.getContacts(); // manager ("report_to")
        }
        return chain;
    }

    /**
     * Builds tree:
     * - children = all direct reports of current node
     * - only the "next node on the path" is expanded recursively
     * - all other children become leaf nodes (children=null)
     */
    private ContactNodeDto buildHierarchyAlongPath(
            Integer currentId,
            Integer ownerId,
            List<Integer> pathIds,
            int nextPathIndex,
            Set<Integer> buildVisited
    ) {
        if (currentId == null) return null;

        // Cycle protection during build as well
        if (!buildVisited.add(currentId)) {
            ContactNodeDto cycleNode = new ContactNodeDto();
            Contacts c = contactsRepository.findById(currentId).orElse(null);
            cycleNode.id = currentId;
            cycleNode.name = (c != null) ? fullName(c) : "Unknown";
            cycleNode.title = (c != null) ? c.getTitle() : null;
            cycleNode.children = null; // stop expansion
            return cycleNode;
        }

        Contacts current = contactsRepository.findById(currentId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + currentId));

        ContactNodeDto node = new ContactNodeDto();
        node.id = current.getId();
        node.name = fullName(current);
        node.title = current.getTitle();

        // Direct reports (siblings group at this level)
        List<Contacts> reports = findDirectReports(ownerId, currentId);

        // Sort for stable UI (optional)
        reports.sort(Comparator.comparing(this::fullName, String.CASE_INSENSITIVE_ORDER));

        if (reports.isEmpty()) {
            node.children = null;
            return node;
        }

        Integer nextOnPath = (nextPathIndex < pathIds.size()) ? pathIds.get(nextPathIndex) : null;

        List<ContactNodeDto> childDtos = new ArrayList<>(reports.size());
        for (Contacts r : reports) {
            if (nextOnPath != null && r.getId() != null && r.getId().equals(nextOnPath)) {
                // expand only the branch that leads to the requested contact
                childDtos.add(buildHierarchyAlongPath(r.getId(), ownerId, pathIds, nextPathIndex + 1, buildVisited));
            } else {
                // sibling as leaf
                ContactNodeDto leaf = new ContactNodeDto();
                leaf.id = r.getId();
                leaf.name = fullName(r);
                leaf.title = r.getTitle();
                leaf.children = null;
                childDtos.add(leaf);
            }
        }

        node.children = childDtos.isEmpty() ? null : childDtos;
        return node;
    }

    /**
     * ✅ Efficient direct-reports fetch.
     * <p>
     * REQUIRED repository method (add it in ContactsRepository):
     * List<Contacts> findByIsDeletedFalseAndCustomersIdAndContactsId(Integer customersId, Integer contactsId);
     * <p>
     * If you want cross-owner hierarchy, remove customersId filter and keep only contactsId.
     */
    private List<Contacts> findDirectReports(Integer ownerId, Integer managerContactId) {
        if (managerContactId == null) return Collections.emptyList();

        // If ownerId is null, fall back to manager-only filter (depends on your data model needs)
        if (ownerId == null) {
            // You can create another repo method: findByIsDeletedFalseAndContactsId(Integer contactsId)
            // For now: safest is empty to avoid leaking other users' data
            return Collections.emptyList();
        }

        return contactsRepository.findByIsDeletedFalseAndCustomersIdAndContactsId(ownerId, managerContactId);
    }

    private String fullName(Contacts c) {
        String fn = c.getFirstName();
        String ln = c.getLastName();
        String name = ((fn == null ? "" : fn.trim()) + " " + (ln == null ? "" : ln.trim())).trim();
        if (name.isEmpty()) name = "Unknown";
        return name;
    }

    private Map<String, Object> toMap(ContactNodeDto node) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", node.id);
        m.put("name", node.name);
        m.put("title", node.title);

        if (node.children == null || node.children.isEmpty()) {
            m.put("children", null);
        } else {
            List<Map<String, Object>> kids = new ArrayList<>(node.children.size());
            for (ContactNodeDto ch : node.children) kids.add(toMap(ch));
            m.put("children", kids);
        }
        return m;
    }

    private Map<String, Object> toMapLite(Contacts c, List<Map<String, Object>> children) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", fullName(c));
        m.put("title", c.getTitle());
        m.put("children", children == null || children.isEmpty() ? null : children);
        return m;
    }
}
