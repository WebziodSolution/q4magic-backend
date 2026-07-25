package com.q4magic.common.salesforce.syncFromQ4Magic;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.models.SyncRecordsQueue;
import com.q4magic.common.repository.SyncRecordsQueueRepository;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/syncFromQ4Magic")
public class SyncFromQ4MagicController {
    @Autowired
    public SyncFromQ4MagicService syncFromQ4MagicService;

    @Autowired
    private SyncRecordsQueueRepository syncRecordsQueueRepository;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @RequestMapping
    public ApiResponse<Map<String, Object>> syncFromQ4Magic(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam("access_token") String accessToken, @RequestParam("instance_url") String instanceUrl) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            List<SyncRecordsQueue> syncRecordsQueues =
                    this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "Account");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncAccounts(accessToken, instanceUrl, userId);
            }
            syncRecordsQueues = this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "Opportunities");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncOpportunity(accessToken, instanceUrl, userId);
            }
            syncRecordsQueues = this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "Contact");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncContacts(accessToken, instanceUrl, userId);
            }
            syncRecordsQueues = this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "OpportunitiesPartner");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncOpportunityPartner(accessToken, instanceUrl, userId);
            }
            syncRecordsQueues = this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "OpportunitiesContacts");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncOpportunityContacts(accessToken, instanceUrl, userId);
            }
            syncRecordsQueues = this.syncRecordsQueueRepository.findByUserIdAndSubject(userId, "OpportunitiesProducts");
            if (!syncRecordsQueues.isEmpty()) {
                this.syncFromQ4MagicService.syncOpportunityProducts(accessToken, instanceUrl, userId);
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Sync successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }
}
