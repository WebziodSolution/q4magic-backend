package com.q4magic.oportunities.serviceImpl;

import com.q4magic.closePlan.service.ClosePlanService;
import com.q4magic.closePlanNotes.service.ClosePlanNotesService;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.common.specification.OpportunitiesSpecification;
import com.q4magic.docsCategory.service.DocsCategoryService;
import com.q4magic.oportunities.service.OpportunitiesService;
import com.q4magic.opportunitiesCurrentEnvironment.service.OpportunitiesCurrentEnvironmentService;
import com.q4magic.opportunityContactNotes.service.OpportunityContactNotesService;
import com.q4magic.opportunityPartnerDetails.service.OpportunityPartnerDetailsService;
import com.q4magic.salesProcess.service.SalesProcessService;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import com.q4magic.todo.service.TodoService;
import com.q4magic.todo.serviceImpl.TodoServiceImpl;
import com.q4magic.todoAssign.service.TodoAssignService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

@Service(value = "opportunitiesService")
public class OpportunitiesServiceImpl implements OpportunitiesService {

    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private MeetingSummaryRepository meetingSummaryRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private OpportunityPartnerDetailsService opportunityPartnerDetailsService;

    @Autowired
    private OpportunityLineItemsRepository opportunityLineItemsRepository;

    @Autowired
    private OpportunityProductsRepository opportunityProductsRepository;

    @Autowired
    private ClosePlanService closePlanService;

    @Autowired
    private ClosePlanNotesService closePlanNotesService;

    @Autowired
    private DocsCategoryService docsCategoryService;

    @Autowired
    private OpportunitiesCurrentEnvironmentService opportunitiesCurrentEnvironmentService;

    @Autowired
    private OpportunitiesCurrentEnvironmentRepository opportunitiesCurrentEnvironmentRepository;

    @Autowired
    private SalesProcessService salesProcessService;

    @Autowired
    private ProcessNameRepository processNameRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoAssignService todoAssignService;

    @Override
    public List<OpportunitiesDto> getAllOpportunities(Integer userId, String fetchType, String search,
            List<String> salesStages, List<String> status) {
        try {
            Customers me = customersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            Set<Integer> visibleCustomerIds = new HashSet<>();
            visibleCustomerIds.add(userId);

            if (me.getRole() != null && "SALE MANAGER".equals(me.getRole().getRole())) {
                List<Customers> subs = customersRepository.getAllSubUsers(userId);
                if (subs != null)
                    for (Customers s : subs)
                        visibleCustomerIds.add(s.getId());
            }
            // else if (me.getCustomers() != null) {
            // System.out.println("========== else =============");
            // visibleCustomerIds.add(me.getCustomers().getId());
            // }
            Specification<Opportunities> spec = Specification.allOf(
                    OpportunitiesSpecification.notDeleted(),
                    OpportunitiesSpecification.customerIdIn(visibleCustomerIds),
                    OpportunitiesSpecification.search(search),
                    OpportunitiesSpecification.salesStageIn(salesStages),
                    OpportunitiesSpecification.statusIn(status));

            List<Opportunities> rows = opportunitiesRepository.findAll(
                    spec, Sort.by(Sort.Direction.DESC, "id"));

            return rows.stream()
                    .map(o -> getOpportunityById(o.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllOpportunitiesGroupedByStage(
            Integer userId,
            String fetchType,
            String search,
            List<String> salesStages,
            List<String> status) {
        try {
            List<OpportunitiesDto> flatList = getAllOpportunities(
                    userId,
                    fetchType,
                    search,
                    salesStages,
                    status);

            for (OpportunitiesDto opp : flatList) {
                if (opp.getId() != null) {
                    List<ClosePlanDto> closePlanDtoList = this.closePlanService.getClosePlanByOppId(opp.getId());

                    opp.setClosePlanDtoList(closePlanDtoList);
                }
            }
            // NEW FIXED ORDER BASED ON opportunityStatus (frontend)
            List<String> fixedStatusOrder = Arrays.asList(
                    "Commit",
                    "Upside",
                    "Pipeline",
                    "Won");

            // Group by status instead of sales stage
            Map<String, List<OpportunitiesDto>> tempGroup = new HashMap<>();

            for (OpportunitiesDto dto : flatList) {
                String oppStatus = dto.getStatus();

                if (oppStatus == null || oppStatus.isBlank())
                    continue;

                tempGroup.computeIfAbsent(oppStatus, k -> new ArrayList<>()).add(dto);
            }

            List<Map<String, Object>> result = new ArrayList<>();

            // FUNCTION TO CALCULATE TOTAL
            java.util.function.Function<List<OpportunitiesDto>, Integer> calcTotal = list -> list.stream()
                    .map(OpportunitiesDto::getDealAmount)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            // Add groups by fixed status order
            for (String st : fixedStatusOrder) {
                if (tempGroup.containsKey(st)) {

                    List<OpportunitiesDto> list = tempGroup.get(st);
                    double total = calcTotal.apply(list);

                    Map<String, Object> obj = new HashMap<>();
                    obj.put("statusname", st); // 🔥 renamed for clarity
                    obj.put("data", list);
                    obj.put("total", String.format("%.2f", total));

                    result.add(obj);
                }
            }

            // Add any other statuses not in fixed order (rare)
            for (Map.Entry<String, List<OpportunitiesDto>> entry : tempGroup.entrySet()) {
                if (!fixedStatusOrder.contains(entry.getKey())) {

                    double total = calcTotal.apply(entry.getValue());

                    Map<String, Object> obj = new HashMap<>();
                    obj.put("statusname", entry.getKey());
                    obj.put("data", entry.getValue());
                    obj.put("total", String.format("%.2f", total));

                    result.add(obj);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getOpportunityOptions(Integer userId) {
        try {
            Customers me = customersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // who this user can see
            Set<Integer> visibleCustomerIds = new HashSet<>();
            visibleCustomerIds.add(userId);

            if (me.getRole() != null && "SALE MANAGER".equals(me.getRole().getRole())) {
                List<Customers> subs = customersRepository.getAllSubUsers(userId);
                if (subs != null)
                    for (Customers s : subs)
                        visibleCustomerIds.add(s.getId());
            }

            // fetch all visible, not-deleted opportunities (latest first)
            var spec = Specification.allOf(
                    OpportunitiesSpecification.notDeleted(),
                    OpportunitiesSpecification.customerIdIn(visibleCustomerIds));
            List<Opportunities> rows = opportunitiesRepository.findAll(
                    spec, Sort.by(Sort.Direction.DESC, "id"));

            // ---------- OPPORTUNITY NAME OPTIONS ----------
            // Use the actual opportunity ID – do NOT deduplicate.
            // If the same name appears multiple times, each appears as a separate option.
            List<Map<String, Object>> nameOptions = new ArrayList<>();
            for (Opportunities o : rows) {
                if (o.getOpportunity() != null && !o.getOpportunity().isBlank()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("title", o.getOpportunity());
                    m.put("logo", o.getLogo());
                    nameOptions.add(m);
                }
            }

            // ---------- SALES STAGE OPTIONS (deduped, not used by frontend yet) ----------
            Set<String> seenStages = new LinkedHashSet<>();
            for (Opportunities o : rows) {
                if (o.getSalesStage() != null && !o.getSalesStage().isBlank()) {
                    seenStages.add(o.getSalesStage());
                }
            }
            List<Map<String, Object>> stageOptions = new ArrayList<>();
            int idx = 1;
            for (String title : seenStages) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", idx++); // sequential id, not used elsewhere
                m.put("title", title);
                stageOptions.add(m);
            }

            // Payload shape expected by the frontend
            Map<String, Object> payload = new HashMap<>();
            payload.put("opportunitiesNameOptions", nameOptions);
            payload.put("opportunitiesStagesOptions", stageOptions);

            return Collections.singletonList(payload);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesDto getOpportunityById(Integer id) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
            OpportunitiesDto opportunitiesDto = new OpportunitiesDto();
            if (opportunities.getCloseDate() != null) {
                opportunitiesDto.setCloseDate(this.commonService.convertDateToString(opportunities.getCloseDate()));
            }
            if (opportunities.getForecastDate() != null) {
                opportunitiesDto
                        .setForecastDate(this.commonService.convertDateToString(opportunities.getForecastDate()));
            }
            if (opportunities.getAccount() != null) {
                opportunitiesDto.setAccountId(opportunities.getAccount().getId());
                opportunitiesDto.setAccountName(opportunities.getAccount().getAccountName());
            }
            if (opportunities.getCreatedAt() != null) {
                opportunitiesDto.setCreatedAt(this.commonService.convertDateToString(opportunities.getCreatedAt()));
            }
            List<OpportunityPartnerDetailsDto> partnerDetails = this.opportunityPartnerDetailsService
                    .getAllOpportunityPartnerDetails(opportunities.getId());
            opportunitiesDto.setOpportunityPartnerDetails(partnerDetails);

            opportunitiesDto.setCreatedBy(opportunities.getCustomers().getId());
            BeanUtils.copyProperties(opportunities, opportunitiesDto);

            List<ClosePlanDto> closePlanDtoList = this.closePlanService.getClosePlanByOppId(opportunities.getId());
            opportunitiesDto.setClosePlanDtoList(closePlanDtoList);

            if (!closePlanDtoList.isEmpty()) {
                for (ClosePlanDto closePlanDto : closePlanDtoList) {
                    List<ClosePlanNotesDto> closePlanNotesDtoList = this.closePlanNotesService
                            .findLastTwoComments(closePlanDto.getId(), closePlanDto.getContactId());
                    if (!closePlanNotesDtoList.isEmpty()) {
                        opportunitiesDto.setClosePlanCommentDto(closePlanNotesDtoList);
                        break;
                    }
                }
            }

            return opportunitiesDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesDto createOpportunity(OpportunitiesDto opportunity, Boolean syncToSalesforce) {
        try {
            Opportunities opportunities = new Opportunities();
            opportunities.setCreatedAt(new Date());

            if (opportunity.getAccountId() != null) {
                Account account = this.accountRepository.findById(opportunity.getAccountId())
                        .orElseThrow(
                                () -> new RuntimeException("Account not found with id: " + opportunity.getAccountId()));

                opportunities.setAccount(account);
            } else {
                opportunities.setAccount(null);
            }

            Customers customers = this.customersRepository.findById(opportunity.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + opportunity.getCreatedBy()));

            opportunities.setCustomers(customers);
            opportunities.setIsDeleted(false);

            if (opportunity.getDealAmount() != null) {

                int listPrice = opportunity.getListPrice();
                int dealAmount = opportunity.getDealAmount();

                // Integer-safe percentage calculation
                int pct = ((listPrice - dealAmount) * 100) / listPrice;

                // Clamp between 0–100
                if (pct < 0)
                    pct = 0;
                if (pct > 100)
                    pct = 100;

                opportunities.setDiscountPercentage(pct);
                opportunities.setDealAmount(dealAmount); // already integer
            } else {
                opportunities.setDiscountPercentage(0);
                opportunities.setDealAmount(
                        opportunity.getListPrice() != null ? opportunity.getListPrice() : 0);
            }

            if (opportunity.getCloseDate() != null) {
                opportunities.setCloseDate(this.commonService.convertStringToDate(opportunity.getCloseDate()));
            }
            if (opportunity.getForecastDate() != null) {
                opportunities.setForecastDate(this.commonService.convertStringToDate(opportunity.getForecastDate()));
            }
            if (opportunity.getLogo() != null) {
                opportunities.setLogo(opportunity.getLogo());
            }
            BeanUtils.copyProperties(opportunity, opportunities, "id", "isDeleted", "logo", "dealAmount",
                    "discountPercentage");
            this.opportunitiesRepository.save(opportunities);

            if (opportunity.getNewLogo() != null) {
                String updatedPath = this.commonService.updateFileLocation(opportunity.getNewLogo(),
                        opportunities.getCustomers().getId(), "oppLogo", "oppLogo/" + opportunities.getId());
                if (!updatedPath.equals("Error")) {
                    opportunities.setLogo(updatedPath);
                    this.opportunitiesRepository.save(opportunities);
                }
            }
            opportunity.setId(opportunities.getId());

            if (syncToSalesforce) {
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunities.getId());
                syncRecordsQueueDto.setSubject("Opportunities");
                syncRecordsQueueDto.setOperationType("CREATE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(opportunity.getCreatedBy());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            }

            if (opportunities.getStatus().equals("Won")) {
                List<OpportunityProducts> opportunityProductsList = this.opportunityProductsRepository
                        .getByOppId(opportunities.getId());
                if (!opportunityProductsList.isEmpty()) {
                    for (OpportunityProducts oppProduct : opportunityProductsList) {
                        OpportunityLineItems opportunityLineItems = new OpportunityLineItems();
                        opportunityLineItems.setOpportunities(opportunities);
                        opportunityLineItems.setProducts(oppProduct.getProducts());
                        opportunityLineItems.setType(oppProduct.getProducts().getType());
                        opportunityLineItems.setName(oppProduct.getProducts().getName());
                        this.opportunityLineItemsRepository.save(opportunityLineItems);
                    }
                }
            }

            for (String categoryName : Constants.DEFAULT_CATEGORY_NAMES) {
                DocsCategoryDto docsCategoryDto = new DocsCategoryDto();
                docsCategoryDto.setCategoryName(categoryName);
                docsCategoryDto.setOppId(opportunities.getId());
                this.docsCategoryService.save(docsCategoryDto);
            }
            OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto = new OpportunitiesCurrentEnvironmentDto();
            opportunitiesCurrentEnvironmentDto.setOppId(opportunities.getId());
            opportunitiesCurrentEnvironmentDto.setSolution("CRM");
            opportunitiesCurrentEnvironmentDto.setVendors(
                    "[{\"isChecked\":false,\"value\":\"HubSpot\"},{\"isChecked\":false,\"value\":\"SalesForce\"}]");
            this.opportunitiesCurrentEnvironmentService
                    .addOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironmentDto);

            OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto2 = new OpportunitiesCurrentEnvironmentDto();
            opportunitiesCurrentEnvironmentDto2.setOppId(opportunities.getId());
            opportunitiesCurrentEnvironmentDto2.setSolution("Competitors");
            opportunitiesCurrentEnvironmentDto2.setVendors("");
            this.opportunitiesCurrentEnvironmentService
                    .addOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironmentDto2);
            return opportunity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesDto updateOpportunity(Integer id, OpportunitiesDto opportunity, Boolean syncToSalesforce) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
            if (opportunity.getAccountId() != null) {
                Account account = accountRepository.findById(opportunity.getAccountId())
                        .orElseThrow(
                                () -> new RuntimeException("Account not found with id: " + opportunity.getAccountId()));

                opportunities.setAccount(account);
            } else {
                opportunities.setAccount(null);
            }

            opportunities.setIsDeleted(false);
            if (opportunity.getCloseDate() != null) {
                opportunities.setCloseDate(this.commonService.convertStringToDate(opportunity.getCloseDate()));
            }

            if (opportunity.getForecastDate() != null) {
                opportunities.setForecastDate(this.commonService.convertStringToDate(opportunity.getForecastDate()));
            }

            if (opportunity.getDealAmount() != null && opportunity.getDealAmount() != 0) {

                int listPrice = (opportunity.getListPrice() != null && opportunity.getListPrice() != 0) ? opportunity.getListPrice() : opportunity.getDealAmount();
                int dealAmount = opportunity.getDealAmount();

                // Integer-safe percentage calculation
                int pct = ((listPrice - dealAmount) * 100) / listPrice;

                // Clamp between 0–100
                if (pct < 0)
                    pct = 0;
                if (pct > 100)
                    pct = 100;

                opportunities.setDiscountPercentage(pct);
                opportunities.setDealAmount(dealAmount); // already integer
            } else {
                opportunities.setDiscountPercentage(0);
                opportunities.setDealAmount(
                        opportunity.getListPrice() != null ? opportunity.getListPrice() : 0);
            }
            opportunities.setWhyDoAnything(opportunity.getWhyDoAnything());
            opportunities.setBusinessValue(opportunity.getBusinessValue());
            opportunities.setCurrentEnvironment(opportunity.getCurrentEnvironment());
            opportunities.setDecisionMap(opportunity.getDecisionMap());
            opportunities.setDecisionCriteria(opportunity.getDecisionCriteria());

            BeanUtils.copyProperties(opportunity, opportunities, "id", "isDeleted", "dealAmount", "discountPercentage",
                    "whyDoAnything", "businessValue", "currentEnvironment", "decisionMap");
            this.opportunitiesRepository.save(opportunities);

            if (syncToSalesforce && opportunities.getSalesforceOpportunityId() != null) {
                SyncRecordsQueue syncRecordsQueue = this.syncRecordsQueueRepository.findBySubjectId(id,
                        opportunity.getCreatedBy());
                if (syncRecordsQueue == null) {
                    SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                    syncRecordsQueueDto.setSubjectId(opportunities.getId());
                    syncRecordsQueueDto.setSubject("Opportunities");
                    syncRecordsQueueDto.setOperationType("UPDATE");
                    syncRecordsQueueDto.setSyncType("PUSH");
                    syncRecordsQueueDto.setDeleted(false);
                    syncRecordsQueueDto.setCreatedBy(opportunities.getCustomers().getId());
                    this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
                }
            }
            if (opportunities.getStatus().equals("Won")) {
                List<OpportunityProducts> opportunityProductsList = this.opportunityProductsRepository
                        .getByOppId(opportunities.getId());
                if (!opportunityProductsList.isEmpty()) {
                    for (OpportunityProducts oppProduct : opportunityProductsList) {
                        OpportunityLineItems opportunityLineItems = new OpportunityLineItems();
                        opportunityLineItems.setOpportunities(opportunities);
                        opportunityLineItems.setProducts(oppProduct.getProducts());
                        opportunityLineItems.setType(oppProduct.getProducts().getType());
                        opportunityLineItems.setName(oppProduct.getProducts().getName());
                        this.opportunityLineItemsRepository.save(opportunityLineItems);
                    }
                }
            }
            if (opportunity.getNewLogo() != null) {
                String updatedPath = this.commonService.updateFileLocation(opportunity.getNewLogo(),
                        opportunities.getCustomers().getId(), "oppLogo", "oppLogo/" + opportunities.getId());
                if (!updatedPath.equals("Error")) {
                    opportunities.setLogo(updatedPath);
                    this.opportunitiesRepository.save(opportunities);
                }
            }
            opportunity.setId(id);
            return opportunity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOpportunity(Integer id, Boolean syncToSalesforce) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
            if (syncToSalesforce && opportunities.getSalesforceOpportunityId() != null) {
                opportunities.setIsDeleted(true);
                this.opportunitiesRepository.save(opportunities);
                SyncRecordsQueueDto syncRecordsQueueDto = new SyncRecordsQueueDto();
                syncRecordsQueueDto.setSubjectId(opportunities.getId());
                syncRecordsQueueDto.setSubject("Opportunities");
                syncRecordsQueueDto.setOperationType("DELETE");
                syncRecordsQueueDto.setSyncType("PUSH");
                syncRecordsQueueDto.setDeleted(false);
                syncRecordsQueueDto.setCreatedBy(opportunities.getCustomers().getId());
                this.syncRecordsQueueService.createSyncRecord(syncRecordsQueueDto);
            } else {
                SyncRecordsQueueDto existingSyncRecord = this.syncRecordsQueueService.findBySubjectId(id,
                        opportunities.getCustomers().getId());
                if (existingSyncRecord != null) {
                    this.syncRecordsQueueService.deleteSyncRecord(existingSyncRecord.getId());
                }
                this.opportunitiesRepository.delete(opportunities);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteOpportunityLogo(Integer id, Integer userId) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
            File existingImagePath = new File(FILE_DIRECTORY + userId + "/oppLogo/" + id);
            if (existingImagePath.exists()) {
                this.commonService.deleteDirectoryRecursively(existingImagePath);
                opportunities.setLogo(null);
                this.opportunitiesRepository.save(opportunities);
                return true;
            } else {
                if (opportunities.getLogo().startsWith("https://cdn.brandfetch.io")) {
                    opportunities.setLogo(null);
                    this.opportunitiesRepository.save(opportunities);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateOpportunityDeal(Integer id, Integer dealAmount) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
            opportunities.setDealAmount(dealAmount);
            this.opportunitiesRepository.save(opportunities);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String updateOpportunityLogo(Integer userId, Integer oppId, String image) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(oppId)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found"));
            String updatedPath = this.commonService.updateFileLocation(image, userId, "oppLogo", "oppLogo/" + oppId);
            if (!updatedPath.equals("Error")) {
                opportunities.setLogo(updatedPath);
                this.opportunitiesRepository.save(opportunities);
            }
            return opportunities.getLogo();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> updateOpportunityData(Map<String, Object> data) {
        try {
            Map<String, Object> res = new HashMap<>();
            Integer oppId = data.get("opportunityId") != null ? Integer.parseInt(data.get("opportunityId").toString())
                    : 0;

            // Support both "customerId" and "customerID"
            Object customerIdObj = data.get("customerId");
            if (customerIdObj == null) {
                customerIdObj = data.get("customerID");
            }
            int businessValueStatus = Integer.parseInt(data.get("businessValueStatus").toString());
            int whyDoAnythingStatus = Integer.parseInt(data.get("whyDoAnythingStatus").toString());
            int nextStepsStatus = Integer.parseInt(data.get("nextStepsStatus").toString());
            int currentEnvironmentStatus = Integer.parseInt(data.get("currentEnvironmentStatus").toString());
            int opportunityContactStatus = Integer.parseInt(data.get("opportunityContactStatus").toString());

            Integer cusId = customerIdObj != null ? Integer.parseInt(customerIdObj.toString()) : 0;
            List<Map<String, Object>> currentEnv = data.get("currentEnv") != null
                    ? (List<Map<String, Object>>) data.get("currentEnv")
                    : null;
            List<Map<String, Object>> decisionMap = null;
            Object decisionMapObj = data.get("DecisionMap");
            if (decisionMapObj instanceof List) {
                decisionMap = (List<Map<String, Object>>) decisionMapObj;
            } else if (decisionMapObj instanceof Map) {
                Map<?, ?> tempMap = (Map<?, ?>) decisionMapObj;
                if (tempMap.containsKey("result") && tempMap.get("result") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("result");
                } else if (tempMap.containsKey("processes") && tempMap.get("processes") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("processes");
                } else if (tempMap.containsKey("DecisionMap") && tempMap.get("DecisionMap") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("DecisionMap");
                }
            }

            if (oppId != 0 && cusId != 0) {
                Opportunities opportunities = this.opportunitiesRepository.findById(oppId)
                        .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + oppId));
                Customers customers = this.customersRepository.findById(cusId)
                        .orElseThrow(() -> new RuntimeException("Customer not found with id: " + cusId));

                if (currentEnv != null && currentEnvironmentStatus == 0) {
                    List<OpportunitiesCurrentEnvironment> opportunitiesCurrentEnvironmentList = this.opportunitiesCurrentEnvironmentRepository
                            .findByOppId(oppId);
                    if (!opportunitiesCurrentEnvironmentList.isEmpty()) {
                        for (OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment : opportunitiesCurrentEnvironmentList) {
                            this.opportunitiesCurrentEnvironmentRepository.delete(opportunitiesCurrentEnvironment);
                        }
                    }
                    for (Map<String, Object> currentEnvMap : currentEnv) {
                        OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto = new OpportunitiesCurrentEnvironmentDto();
                        opportunitiesCurrentEnvironmentDto.setOppId(oppId);
                        opportunitiesCurrentEnvironmentDto.setSolution((String) currentEnvMap.get("solution"));
                        opportunitiesCurrentEnvironmentDto.setVendors((String) currentEnvMap.get("vendors"));
                        this.opportunitiesCurrentEnvironmentService
                                .addOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironmentDto);
                    }
                }
                if (businessValueStatus == 0) {
                    Object businessValueObj = data.get("BusinessValue");
                    opportunities.setBusinessValue(businessValueObj != null ? businessValueObj.toString() : null);
                }
                if (whyDoAnythingStatus == 0) {
                    Object whyObj = data.get("Why_Do_Anything");
                    opportunities.setWhyDoAnything(whyObj != null ? whyObj.toString() : null);
                }
                if (nextStepsStatus == 0) {
                    opportunities.setNextSteps(data.get("NextSteps") != null ? data.get("NextSteps").toString() : null);
                }

                this.opportunitiesRepository.save(opportunities);

                // Handle optional KeyContacts
                if (opportunityContactStatus == 0) {
                    List<OpportunityContact> opportunityContactList = this.opportunityContactRepository
                            .findByOppId(oppId);
                    if (!opportunityContactList.isEmpty()) {
                        for (OpportunityContact opportunityContact : opportunityContactList) {
                            this.opportunityContactRepository.delete(opportunityContact);
                        }
                    }

                    Object keyContactsObj = data.get("KeyContacts");
                    if (keyContactsObj instanceof List) {
                        List<Map<String, Object>> contactsList = (List<Map<String, Object>>) keyContactsObj;

                        for (Map<String, Object> map : contactsList) {
                            String firstName = (String) map.get("firstName");
                            String lastName = (String) map.get("lastName");
                            String title = (String) map.get("title");
                            String role = (String) map.get("role");

                            Contacts contact = new Contacts();
                            contact.setFirstName(firstName);
                            contact.setLastName(lastName);
                            contact.setTitle(title);
                            contact.setRole((role != null && !role.isBlank()) ? role : title);
                            contact.setFromMailScraping(false);
                            contact.setCustomers(customers);
                            contact.setIsDeleted(false);
                            this.contactsRepository.save(contact);

                            OpportunityContact opportunityContact = new OpportunityContact();
                            opportunityContact.setContacts(contact);
                            opportunityContact.setOpportunities(opportunities);
                            opportunityContact.setTitle(title);
                            opportunityContact.setRole((role != null && !role.isBlank()) ? role : title);
                            opportunityContact.setIsKey(true);
                            opportunityContact.setIsDeleted(false);
                            this.opportunityContactRepository.save(opportunityContact);
                        }
                    }
                }

                if (decisionMap != null) {
                    List<SalesProcessDto> salesProcessDtoList = this.salesProcessService.getAllByOpportunity(oppId);
                    if (!salesProcessDtoList.isEmpty()) {
                        for (SalesProcessDto salesProcessDto : salesProcessDtoList) {
                            this.salesProcessService.deleteSaleProcess(salesProcessDto.getId());
                        }
                    }
                    List<ProcessName> processNameList = this.processNameRepository.findByOppId(oppId);
                    if (!processNameList.isEmpty()) {
                        for (ProcessName processName : processNameList) {
                            this.processNameRepository.delete(processName);
                        }
                    }
                    for (Map<String, Object> decision : decisionMap) {
                        String contact_name = decision.get("contact_name") != null
                                ? decision.get("contact_name").toString()
                                : null;
                        String process_name = decision.get("process_name") != null
                                ? decision.get("process_name").toString()
                                : null;
                        String process_date = decision.get("process_date") != null
                                ? decision.get("process_date").toString()
                                : null;

                        ProcessName processName = this.processNameRepository.findByOppIdAndName(oppId, process_name);
                        if (processName == null) {
                            processName = new ProcessName();
                            processName.setName(process_name);
                            processName.setCustomers(customers);
                            processName.setOpportunities(opportunities);
                            this.processNameRepository.save(processName);
                        }

                        // 1. Fetch as a List to prevent crashes
                        List<Contacts> matchingContacts = contact_name != null
                                ? this.contactsRepository.getByFirstOrLastName(cusId, contact_name)
                                : Collections.emptyList();

                        SalesProcessDto salesProcessDto = new SalesProcessDto();
                        salesProcessDto.setProcess(process_name);
                        salesProcessDto.setProcessDate(process_date);
                        salesProcessDto.setContactId(null);

                        //// 2. Safely grab the first match if any exist, otherwise set null
                        // if (!matchingContacts.isEmpty() && matchingContacts.size() > 0) {
                        // // Optionally log a warning if matchingContacts.size() > 1
                        // salesProcessDto.setContactId(matchingContacts.get(0).getId());
                        // } else {
                        // salesProcessDto.setContactId(null);
                        // }

                        salesProcessDto.setOppId(oppId);
                        salesProcessDto.setNotes(null);
                        salesProcessDto.setGoLive(null);
                        salesProcessDto.setReason(null);

                        this.salesProcessService.createSaleProcess(salesProcessDto);
                    }
                }

                if (data.get("storeNote") != null && data.get("storeNote").equals("Y")) {
                    MeetingSummary meetingSummary = new MeetingSummary();
                    meetingSummary.setOpportunities(opportunities);
                    meetingSummary.setCustomers(customers);
                    meetingSummary.setTranscript((String) data.get("cleanTranscript"));

                    // Remove redundant large fields like cleanTranscript from the summary JSON
                    Map<String, Object> summaryData = new HashMap<>(data);
                    summaryData.remove("cleanTranscript");

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String jsonSummary = mapper.writeValueAsString(summaryData);
                    Object nextStepsObj = data.get("nextSteps");
                    if (nextStepsObj == null) {
                        nextStepsObj = data.get("NextSteps");
                    }

                    String nextStepData = "";
                    if (nextStepsObj instanceof List) {
                        StringBuilder sb = new StringBuilder();
                        for (Object item : (List<?>) nextStepsObj) {
                            if (item != null) {
                                if (sb.length() > 0) {
                                    sb.append("\n"); // Joins each next step with a new line
                                }
                                sb.append(item.toString());
                            }
                        }
                        nextStepData = sb.toString();
                    } else if (nextStepsObj != null) {
                        nextStepData = nextStepsObj.toString();
                    }
                    opportunities.setNextSteps(nextStepData);
                    meetingSummary.setSummary(jsonSummary);
                    meetingSummary.setCreatedDate(new Date());
                    this.meetingSummaryRepository.save(meetingSummary);
                    this.opportunitiesRepository.save(opportunities);
                }
            } else {
                res.put("error", "Opportunity and Customer is required.");
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> updateLastOpportunityData(Map<String, Object> data) {
        try {

            Map<String, Object> res = new HashMap<>();
            Integer oppId = data.get("opportunityId") != null ? Integer.parseInt(data.get("opportunityId").toString())
                    : 0;

            // Support both "customerId" and "customerID"
            Object customerIdObj = data.get("customerId");
            if (customerIdObj == null) {
                customerIdObj = data.get("customerID");
            }

            Integer cusId = customerIdObj != null ? Integer.parseInt(customerIdObj.toString()) : 0;
            List<Map<String, Object>> currentEnv = data.get("currentEnv") != null
                    ? (List<Map<String, Object>>) data.get("currentEnv")
                    : null;
            List<Map<String, Object>> decisionMap = null;
            Object decisionMapObj = data.get("DecisionMap");
            if (decisionMapObj instanceof List) {
                decisionMap = (List<Map<String, Object>>) decisionMapObj;
            } else if (decisionMapObj instanceof Map) {
                Map<?, ?> tempMap = (Map<?, ?>) decisionMapObj;
                if (tempMap.containsKey("result") && tempMap.get("result") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("result");
                } else if (tempMap.containsKey("processes") && tempMap.get("processes") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("processes");
                } else if (tempMap.containsKey("DecisionMap") && tempMap.get("DecisionMap") instanceof List) {
                    decisionMap = (List<Map<String, Object>>) tempMap.get("DecisionMap");
                }
            }
            if (oppId != 0 && cusId != 0) {
                Opportunities opportunities = this.opportunitiesRepository.findById(oppId)
                        .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + oppId));
                Customers customers = this.customersRepository.findById(cusId)
                        .orElseThrow(() -> new RuntimeException("Customer not found with id: " + cusId));

                List<OpportunityContact> opportunityContactList = this.opportunityContactRepository.findByOppId(oppId);
                if (!opportunityContactList.isEmpty()) {
                    for (OpportunityContact opportunityContact : opportunityContactList) {
                        this.opportunityContactRepository.delete(opportunityContact);
                    }
                }
                List<OpportunitiesCurrentEnvironment> opportunitiesCurrentEnvironmentList = this.opportunitiesCurrentEnvironmentRepository
                        .findByOppId(oppId);
                if (!opportunitiesCurrentEnvironmentList.isEmpty()) {
                    for (OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment : opportunitiesCurrentEnvironmentList) {
                        this.opportunitiesCurrentEnvironmentRepository.delete(opportunitiesCurrentEnvironment);
                    }
                }
                List<SalesProcessDto> salesProcessDtoList = this.salesProcessService.getAllByOpportunity(oppId);
                if (!salesProcessDtoList.isEmpty()) {
                    for (SalesProcessDto salesProcessDto : salesProcessDtoList) {
                        this.salesProcessService.deleteSaleProcess(salesProcessDto.getId());
                    }
                }
                List<ProcessName> processNameList = this.processNameRepository.findByOppId(oppId);
                if (!processNameList.isEmpty()) {
                    for (ProcessName processName : processNameList) {
                        this.processNameRepository.delete(processName);
                    }
                }

                if (currentEnv != null) {
                    for (Map<String, Object> currentEnvMap : currentEnv) {
                        OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto = new OpportunitiesCurrentEnvironmentDto();
                        opportunitiesCurrentEnvironmentDto.setOppId(oppId);
                        opportunitiesCurrentEnvironmentDto.setSolution((String) currentEnvMap.get("solution"));
                        opportunitiesCurrentEnvironmentDto.setVendors((String) currentEnvMap.get("vendors"));
                        this.opportunitiesCurrentEnvironmentService
                                .addOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironmentDto);
                    }
                }
                Object businessValueObj = data.get("BusinessValue");
                opportunities.setBusinessValue(businessValueObj != null ? businessValueObj.toString() : null);

                Object whyObj = data.get("Why_Do_Anything");
                opportunities.setWhyDoAnything(whyObj != null ? whyObj.toString() : null);
                opportunities.setNextSteps(data.get("NextSteps") != null ? data.get("NextSteps").toString() : null);

                this.opportunitiesRepository.save(opportunities);

                // Handle optional KeyContacts
                Object keyContactsObj = data.get("KeyContacts");
                if (keyContactsObj instanceof List) {
                    List<Map<String, Object>> contactsList = (List<Map<String, Object>>) keyContactsObj;

                    for (Map<String, Object> map : contactsList) {
                        String firstName = (String) map.get("firstName");
                        String lastName = (String) map.get("lastName");
                        String title = (String) map.get("title");
                        String role = (String) map.get("role");

                        Contacts contact = new Contacts();
                        contact.setFirstName(firstName);
                        contact.setLastName(lastName);
                        contact.setTitle(title);
                        contact.setRole((role != null && !role.isBlank()) ? role : title);
                        contact.setFromMailScraping(false);
                        contact.setCustomers(customers);
                        contact.setIsDeleted(false);
                        this.contactsRepository.save(contact);

                        OpportunityContact opportunityContact = new OpportunityContact();
                        opportunityContact.setContacts(contact);
                        opportunityContact.setOpportunities(opportunities);
                        opportunityContact.setRole((role != null && !role.isBlank()) ? role : title);
                        opportunityContact.setTitle(title);
                        opportunityContact.setIsKey(true);
                        opportunityContact.setIsDeleted(false);
                        this.opportunityContactRepository.save(opportunityContact);
                    }
                }

                if (decisionMap != null) {
                    for (Map<String, Object> decision : decisionMap) {
                        String contact_name = decision.get("contact_name") != null
                                ? decision.get("contact_name").toString()
                                : null;
                        String process_name = decision.get("process_name") != null
                                ? decision.get("process_name").toString()
                                : null;
                        String process_date = decision.get("process_date") != null
                                ? decision.get("process_date").toString()
                                : null;

                        ProcessName processName = this.processNameRepository.findByOppIdAndName(oppId, process_name);
                        if (processName == null) {
                            processName = new ProcessName();
                            processName.setName(process_name);
                            processName.setCustomers(customers);
                            processName.setOpportunities(opportunities);
                            this.processNameRepository.save(processName);
                        }

                        // 1. Fetch as a List to prevent crashes
                        List<Contacts> matchingContacts = contact_name != null
                                ? this.contactsRepository.getByFirstOrLastName(cusId, contact_name)
                                : Collections.emptyList();

                        SalesProcessDto salesProcessDto = new SalesProcessDto();
                        salesProcessDto.setProcess(process_name);
                        salesProcessDto.setProcessDate(process_date);
                        salesProcessDto.setContactId(null);
                        //
                        //// 2. Safely grab the first match if any exist, otherwise set null
                        // if (!matchingContacts.isEmpty()) {
                        // // Optionally log a warning if matchingContacts.size() > 1
                        // salesProcessDto.setContactId(matchingContacts.get(0).getId());
                        // } else {
                        // salesProcessDto.setContactId(null);
                        // }

                        salesProcessDto.setOppId(oppId);
                        salesProcessDto.setNotes(null);
                        salesProcessDto.setGoLive(null);
                        salesProcessDto.setReason(null);

                        this.salesProcessService.createSaleProcess(salesProcessDto);
                    }
                }

                // Handle optional KeyContacts
                Object TodoObj = data.get("ToDos");
                if (TodoObj instanceof List) {
                    List<Map<String, Object>> todoList = (List<Map<String, Object>>) TodoObj;
                    for (Map<String, Object> map : todoList) {
                        String related_to = (String) map.get("related_to");
                        String task = (String) map.get("task");
                        String due_date = (String) map.get("due_date");
                        if (due_date != null && !due_date.isEmpty()) {
                            TodoDto todoDto = new TodoDto();
                            todoDto.setRelatedTo(related_to);
                            todoDto.setTask(task);
                            todoDto.setDueDate(due_date);
                            todoDto.setCreatedBy(cusId);
                            TodoDto newTodo = this.todoService.createTodo(todoDto, false);

                            TodoAssignDto todoAssignDto = new TodoAssignDto();
                            todoAssignDto.setTodoId(newTodo.getId());
                            todoAssignDto.setCustomerId(cusId);
                            todoAssignDto.setAssignBy(cusId);
                            todoAssignDto.setDueDate(due_date);
                            this.todoAssignService.assignTodoToUser(todoAssignDto);
                        }
                    }
                }
            } else {
                res.put("error", "Opportunity and Customer is required.");
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> checkOpportunity(Integer oppId) {
        try {
            Map<String, Object> res = new HashMap<>();
            Opportunities opportunities = this.opportunitiesRepository.findById(oppId)
                    .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + oppId));
            res.put("BusinessValue", isEffectivelyEmpty(opportunities.getBusinessValue()) ? 0 : 1);
            res.put("WhyDoAnything", isEffectivelyEmpty(opportunities.getWhyDoAnything()) ? 0 : 1);
            res.put("NextSteps",
                    (opportunities.getNextSteps() == null || opportunities.getNextSteps().isEmpty()) ? 0 : 1);
            List<OpportunitiesCurrentEnvironmentDto> opportunitiesCurrentEnvironment = this.opportunitiesCurrentEnvironmentService
                    .getOpportunitiesCurrentEnvironmentByOppId(oppId);
            List<OpportunityContact> opportunityContact = this.opportunityContactRepository.findByOppId(oppId);
            res.put("CurrentEnvironment",
                    (opportunitiesCurrentEnvironment == null || opportunitiesCurrentEnvironment.isEmpty()) ? 0 : 1);
            res.put("opportunityContact", (opportunityContact == null || opportunityContact.isEmpty()) ? 0 : 1);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer createOpportunityData(Map<String, Object> data) {
        try {
            Integer cusId = (Integer) data.get("customerId");
            String oppName = (String) data.get("oppName");
            String stage = "Prospecting";
            String status = "Pipeline";
            String timezone = (String) data.get("timeZone");
            ZoneId zoneId = ZoneId.of(timezone);

            LocalDate currentDateInZone = LocalDate.now(zoneId);
            LocalDate closeDateCalculated = currentDateInZone.plusDays(3);
            String closeDate = closeDateCalculated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

            Customers customers = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + cusId));
            Opportunities opportunities = new Opportunities();
            opportunities.setCustomers(customers);
            opportunities.setOpportunity(oppName);
            opportunities.setStatus(status);
            opportunities.setSalesStage(stage);
            opportunities.setDiscountPercentage(0);
            opportunities.setListPrice(1);
            opportunities.setDealAmount(1);
            opportunities.setIsDeleted(false);

            if (closeDate != null) {
                opportunities.setCloseDate(this.commonService.convertStringToDate(closeDate));
            }
            this.opportunitiesRepository.save(opportunities);
            return opportunities.getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static boolean isEffectivelyEmpty(String s) {
        if (s == null)
            return true;
        // Remove HTML tags (simple regex, works for basic cases)
        String withoutHtml = s.replaceAll("<[^>]*>", "").trim();
        return withoutHtml.isEmpty();
    }
}
