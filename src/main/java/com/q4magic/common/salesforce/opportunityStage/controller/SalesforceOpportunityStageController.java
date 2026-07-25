package com.q4magic.common.salesforce.opportunityStage.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.opportunityStage.service.SalesforceOpportunityStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce/opportunitystage")
public class SalesforceOpportunityStageController {

    @Autowired
    private SalesforceOpportunityStageService salesforceOpportunityStageService;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllOpportunityStages(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        try {
            Map<String, Object> data = salesforceOpportunityStageService.getAllOpportunityStages(accessToken, instanceUrl);
            return new ApiResponse<>(HttpStatus.OK.value(), "OpportunityStage records fetched successfully", data);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch OpportunityStage records", e.getMessage());
        }
    }
}