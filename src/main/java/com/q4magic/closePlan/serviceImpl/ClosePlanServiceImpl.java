package com.q4magic.closePlan.serviceImpl;

import com.q4magic.closePlan.service.ClosePlanService;
import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.docsAttachments.service.DocsAttachmentsService;
import com.q4magic.docsCategory.service.DocsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.*;

@Service(value = "ClosePlanService")
public class ClosePlanServiceImpl implements ClosePlanService {
    private static final String SECRET_KEY = "your-very-secret-key";

    @Value("${siteUrl}")
    String siteUrl;

    @Autowired
    private ClosePlanRepository closePlanRepository;

    @Autowired
    private ClosePlanNotesRepository closePlanNotesRepository;

    @Autowired
    private SalesProcessRepository salesProcessRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DocsAttachmentsService docsAttachmentsService;

    @Autowired
    private DocsCategoryService docsCategoryService;

    @Override
    public List<ClosePlanDto> getClosePlanByOppId(Integer oppId) {
        try {
            List<ClosePlan> closePlanList = this.closePlanRepository.findByOppId(oppId);
            List<ClosePlanDto> closePlanDtoList = new ArrayList<>();

            if (!closePlanList.isEmpty()) {
                for (ClosePlan closePlan : closePlanList) {
                    ClosePlanDto closePlanDto = new ClosePlanDto();
                    closePlanDto.setId(closePlan.getId());
                    closePlanDto.setOppId(closePlan.getOpportunities().getId());
                    closePlanDto.setCusId(closePlan.getCustomers().getId());
                    closePlanDto.setContactId(closePlan.getContacts().getId());
                    closePlanDto.setContactName(closePlan.getContacts().getFirstName() + " " + closePlan.getContacts().getLastName());
                    closePlanDto.setStatus(closePlan.getStatus());
                    if (closePlan.getStatusTime() != null) {
                        closePlanDto.setStatusTime(this.commonService.convertDateToString(closePlan.getStatusTime()));
                    }
                    if (closePlan.getDate() != null) {
                        closePlanDto.setDate(this.commonService.convertDateToString(closePlan.getDate()));
                    }
                    closePlanDto.setUrl(closePlan.getUrl());
                    List<ClosePlanNotes> closePlanNotesList = this.closePlanNotesRepository.findByClosePlanIdAndContactIds(closePlan.getId(), closePlan.getContacts().getId());
                    closePlanDto.setHasComment(!closePlanNotesList.isEmpty());

                    closePlanDtoList.add(closePlanDto);
                }
            }
            return closePlanDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ClosePlanDto> getClosePlanByOppIdAndStatus(Integer oppId) {
        try {
            List<ClosePlan> closePlanList = this.closePlanRepository.findByOppId(oppId);
            List<ClosePlanDto> closePlanDtoList = new ArrayList<>();
            if (!closePlanList.isEmpty()) {
                for (ClosePlan closePlan : closePlanList) {
                    if (closePlan.getStatus() != null) {
                        ClosePlanDto closePlanDto = new ClosePlanDto();
                        closePlanDto.setId(closePlan.getId());
                        closePlanDto.setOppId(closePlan.getOpportunities().getId());
                        closePlanDto.setCusId(closePlan.getCustomers().getId());
                        closePlanDto.setContactId(closePlan.getContacts().getId());
                        closePlanDto.setContactName(closePlan.getContacts().getFirstName() + " " + closePlan.getContacts().getLastName());
                        closePlanDto.setStatus(closePlan.getStatus());
                        if (closePlan.getStatusTime() != null) {
                            closePlanDto.setStatusTime(this.commonService.convertDateToString(closePlan.getStatusTime()));
                        }
                        if (closePlan.getDate() != null) {
                            closePlanDto.setDate(this.commonService.convertDateToString(closePlan.getDate()));
                        }
                        closePlanDto.setUrl(closePlan.getUrl());
                        List<ClosePlanNotes> closePlanNotesList = this.closePlanNotesRepository.findByClosePlanIdAndContactIds(closePlan.getId(), closePlan.getContacts().getId());
                        closePlanDto.setHasComment(!closePlanNotesList.isEmpty());
                        closePlanDtoList.add(closePlanDto);
                    }

                }
            }
            return closePlanDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ClosePlanUrlResponseDto> saveAndGenerateUrl(Integer cusId, List<ClosePlanDto> closePlanDtoList) {
        try {
            List<ClosePlanUrlResponseDto> response = new ArrayList<>();
            if (!closePlanDtoList.isEmpty()) {
                for (ClosePlanDto closePlanDto : closePlanDtoList) {
                    ClosePlan isExits = this.closePlanRepository.findByOppIdAndContactIdAndCusId(closePlanDto.getOppId(), closePlanDto.getContactId(), cusId);
                    if (isExits == null) {
                        Opportunities opportunities = this.opportunitiesRepository.findById(closePlanDto.getOppId()).orElseThrow(() -> new RuntimeException("Opportunity not found"));
                        Contacts contacts = this.contactsRepository.findById(closePlanDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                        Customers customers = this.customersRepository.findById(cusId).orElseThrow(() -> new RuntimeException("Customer not found"));

                        ClosePlan closePlan = new ClosePlan();
                        closePlan.setContacts(contacts);
                        closePlan.setOpportunities(opportunities);
                        closePlan.setCustomers(customers);
                        closePlan.setDate(new Date());
                        this.closePlanRepository.save(closePlan);

                        String url = generateToken(closePlanDto.getOppId(), closePlanDto.getContactId(), closePlan.getId());
                        closePlan.setUrl(url);
                        this.closePlanRepository.save(closePlan);

                        ClosePlanUrlResponseDto closePlanUrlResponseDto = new ClosePlanUrlResponseDto();
                        closePlanUrlResponseDto.setContactName(contacts.getFirstName() + " " + contacts.getLastName());
                        closePlanUrlResponseDto.setUrl(url);
                        response.add(closePlanUrlResponseDto);
                    } else {
                        String url = generateToken(closePlanDto.getOppId(), closePlanDto.getContactId(), isExits.getId());
                        isExits.setUrl(url);
                        this.closePlanRepository.save(isExits);

                        ClosePlanUrlResponseDto closePlanUrlResponseDto = new ClosePlanUrlResponseDto();
                        closePlanUrlResponseDto.setContactName(isExits.getContacts().getFirstName() + " " + isExits.getContacts().getLastName());
                        closePlanUrlResponseDto.setUrl(url);
                        response.add(closePlanUrlResponseDto);
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
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> res = new HashMap<>();
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(token), java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = decodedToken.split(":");

            if (parts.length != 6) {
                res.put("message", "Invalid token");
                res.put("status", 400);
                return res;
            }

            Integer oppId = Integer.parseInt(parts[0]);
            Integer contactId = Integer.parseInt(parts[1]);
            Integer closePlanId = Integer.parseInt(parts[2]);
            String uuid = parts[3];
            long timestamp = Long.parseLong(parts[4]);
            String providedHmac = parts[5];

            ClosePlan closePlan = this.closePlanRepository.findById(closePlanId)
                    .orElseThrow(() -> new RuntimeException("Close plan not found"));

            String data = oppId + ":" + contactId + ":" + closePlanId + ":" + uuid + ":" + timestamp;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] expectedHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] providedHmacBytes = Base64.getUrlDecoder().decode(providedHmac);

            boolean valid =
                    MessageDigest.isEqual(expectedHmac, providedHmacBytes) &&
                            closePlan.getOpportunities().getId().equals(oppId) &&
                            closePlan.getContacts().getId().equals(contactId);

            if (!valid) {
                res.put("message", "Invalid token");
                res.put("status", 400);
                return res;
            }

            // ✅ only build DTO after token is valid
            ClosePlanResponseDto closePlanResponseDto = buildClosePlanResponseDto(oppId, closePlan);

            res.put("message", "Token is valid");
            res.put("data", closePlanResponseDto);
            res.put("status", 200);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            res.put("message", "Error validating token: " + e.getMessage());
            res.put("status", 500);
            return res;
        }
    }

    @Override
    public void changeStatus(Integer id) {
        ClosePlan closePlan = this.closePlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Close plan not found"));
        closePlan.setStatus("Looks Perfect");
        closePlan.setStatusTime(new Date());
        this.closePlanRepository.save(closePlan);
    }

    private String generateToken(Integer oppId, Integer contactId, Integer closePlanId) throws Exception {
        long currentTimestamp = System.currentTimeMillis();
        String data = oppId + ":" + contactId + ":" + closePlanId + ":" + UUID.randomUUID().toString() + ":" + currentTimestamp;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());

        String token = data + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        String link = Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes());
        return siteUrl + "closeplan/" + link;
    }

    private ClosePlanResponseDto buildClosePlanResponseDto(Integer oppId, ClosePlan closePlan) {

        Opportunities opportunities = this.opportunitiesRepository.findById(oppId)
                .orElseThrow(() -> new RuntimeException("Opp not found"));

        List<SalesProcess> salesProcessList = this.salesProcessRepository.findByOppId(oppId);

        List<OpportunityContact> opportunityContactList =
                this.opportunityContactRepository.findByOppId(oppId);

        ClosePlanResponseDto dto = new ClosePlanResponseDto();
        List<OpportunityContactDto> opportunityContactDtoList = new ArrayList<>();
        List<SalesProcessDto> salesProcessDtoList = new ArrayList<>();
        List<DocsAttachmentsDto> docsAttachmentsDtoList = new ArrayList<>();

        dto.setClosePlanId(closePlan.getId());
        dto.setOppId(opportunities.getId());
        dto.setOppName(opportunities.getOpportunity());
        dto.setNextSteps(opportunities.getNextSteps());
        dto.setBusinessValue(opportunities.getBusinessValue());
        dto.setContactId(closePlan.getContacts().getId());
        dto.setCreatedBy(closePlan.getCustomers().getId());
        dto.setStatus(closePlan.getStatus());
        dto.setContactName(closePlan.getContacts().getFirstName() + " " + closePlan.getContacts().getLastName());
        dto.setCreatedByName(closePlan.getCustomers().getFirstName() + " " + closePlan.getCustomers().getLastName());

        if (!opportunityContactList.isEmpty()) {
            for (OpportunityContact opportunityContact : opportunityContactList) {
                OpportunityContactDto opportunityContactDto = new OpportunityContactDto();
                if (opportunityContact.getIsKey()) {
                    opportunityContactDto.setId(opportunityContact.getId());
                    opportunityContactDto.setContactName(opportunityContact.getContacts().getFirstName() + " " + opportunityContact.getContacts().getLastName());
                    opportunityContactDto.setRole(opportunityContact.getRole());
                    opportunityContactDto.setIsKey(opportunityContact.getIsKey());
                    opportunityContactDtoList.add(opportunityContactDto);
                }
            }
        }

        if (!salesProcessList.isEmpty()) {
            for (SalesProcess salesProcess : salesProcessList) {
                SalesProcessDto salesProcessDto = new SalesProcessDto();
                salesProcessDto.setId(salesProcess.getId());
                salesProcessDto.setProcess(salesProcess.getProcess());
                salesProcessDto.setProcessDate(this.commonService.convertDateToString(salesProcess.getProcessDate()));
                if (salesProcess.getGoLive() != null) {
                    salesProcessDto.setGoLive(this.commonService.convertDateToString(salesProcess.getGoLive()));
                    salesProcessDto.setReason(salesProcess.getReason());
                }
                salesProcessDtoList.add(salesProcessDto);
            }
        }
        List<DocsCategoryDto> docsCategoryDtoList = this.docsCategoryService.getByOppId(oppId);
        if (!docsCategoryDtoList.isEmpty()) {
            for (DocsCategoryDto docsCategoryDto : docsCategoryDtoList) {
                List<DocsAttachmentsDto> docsAttachmentsDtos = this.docsAttachmentsService.findByDocsCategory(docsCategoryDto.getId());
                if (!docsAttachmentsDtos.isEmpty()) {
                    docsAttachmentsDtoList.addAll(docsAttachmentsDtos);
                }
            }
        }
        dto.setDecisionMap(salesProcessDtoList);
        dto.setOpportunityContactDto(opportunityContactDtoList);
        dto.setDocsAttachmentsDtoList(docsAttachmentsDtoList);
        return dto;
    }

}
