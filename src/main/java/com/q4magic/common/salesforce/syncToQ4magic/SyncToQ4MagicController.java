package com.q4magic.common.salesforce.syncToQ4magic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.SyncStatusDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.syncStatus.service.SyncStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/syncToQ4Magic")
public class SyncToQ4MagicController {
    @Autowired
    public SyncToQ4MagicService syncToQ4MagicService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private SyncStatusService syncStatusService;

    @RequestMapping
    public ApiResponse<Map<String, Object>> syncToQ4Magic(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        try {
            SyncStatusDto syncStatusDto = new SyncStatusDto();
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            syncStatusDto.setCustomerId(userId);

            ObjectMapper mapper = new ObjectMapper();

            // helper to extract readable message
            java.util.function.Function<Object, String> extractMessage = (msg) -> {
                if (msg instanceof String) {
                    String str = ((String) msg).trim();
                    if (str.startsWith("[")) {
                        try {
                            List<Map<String, Object>> arr =
                                    mapper.readValue(str, new TypeReference<List<Map<String, Object>>>() {
                                    });
                            if (!arr.isEmpty() && arr.get(0).containsKey("message")) {
                                return arr.get(0).get("message").toString();
                            }
                        } catch (Exception ignore) {
                        }
                    }
                    return str;
                }
                return msg != null ? msg.toString() : "Unknown error";
            };

            syncStatusDto.setStatusMessage("Syncing Accounts.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 1: Accounts
            Map<String, Object> accountResult = this.syncToQ4MagicService.syncAccounts(accessToken, instanceUrl, userId);
            if (accountResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(accountResult.get("message"));
                accountResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) accountResult.getOrDefault("status", 500),
                        cleanMsg,
                        accountResult
                );
            }

            syncStatusDto.setStatusMessage("Syncing Opportunities.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 2: Opportunities
            Map<String, Object> opportunityResult = this.syncToQ4MagicService.syncOpportunity(accessToken, instanceUrl, userId);
            if (opportunityResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(opportunityResult.get("message"));
                opportunityResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) opportunityResult.getOrDefault("status", 500),
                        cleanMsg,
                        opportunityResult
                );
            }

            syncStatusDto.setStatusMessage("Syncing Contacts.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 3: Contacts
            Map<String, Object> contactsResult = this.syncToQ4MagicService.syncContacts(accessToken, instanceUrl, userId);
            if (contactsResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(contactsResult.get("message"));
                contactsResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) contactsResult.getOrDefault("status", 500),
                        cleanMsg,
                        contactsResult
                );
            }
            this.syncToQ4MagicService.setContactIdIntoOpp(accessToken, instanceUrl, userId);

            syncStatusDto.setStatusMessage("Syncing Competitor.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 5: Competitor
            Map<String, Object> competitorResult = this.syncToQ4MagicService.syncCompetitor(accessToken, instanceUrl, userId);
            if (competitorResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(competitorResult.get("message"));
                competitorResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) competitorResult.getOrDefault("status", 500),
                        cleanMsg,
                        competitorResult
                );
            }

            syncStatusDto.setStatusMessage("Syncing Sales Stages.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 6: Stages
            Map<String, Object> stagesResult = this.syncToQ4MagicService.syncStages(accessToken, instanceUrl, userId);
            if (stagesResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(stagesResult.get("message"));
                stagesResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) stagesResult.getOrDefault("status", 500),
                        cleanMsg,
                        stagesResult
                );
            }

            syncStatusDto.setStatusMessage("Syncing Products.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 7: Products
            Map<String, Object> productResult = this.syncToQ4MagicService.syncProducts(accessToken, instanceUrl, userId);
            if (productResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(productResult.get("message"));
                productResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) productResult.getOrDefault("status", 500),
                        cleanMsg,
                        productResult
                );
            }

            syncStatusDto.setStatusMessage("Syncing Opportunities Products.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            // 🔹 Step 8:  Opportunity Products
            Map<String, Object> oppProductResult = this.syncToQ4MagicService.syncOpportunityProducts(accessToken, instanceUrl, userId);
            if (oppProductResult.containsKey("error")) {
                String cleanMsg = extractMessage.apply(oppProductResult.get("message"));
                oppProductResult.put("message", cleanMsg);
                return new ApiResponse<>(
                        (int) oppProductResult.getOrDefault("status", 500),
                        cleanMsg,
                        oppProductResult
                );
            }
            this.syncStatusService.deleteSyncStatus(userId);

            // ✅ Success
            return new ApiResponse<>(HttpStatus.OK.value(), "Sync successfully", Map.of("success", true));

        } catch (Exception e) {
            return new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(),
                    Map.of("success", false, "error", "Exception", "message", e.getMessage())
            );
        }
    }

}
