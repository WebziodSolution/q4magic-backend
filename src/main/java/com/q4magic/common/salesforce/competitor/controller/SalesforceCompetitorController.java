package com.q4magic.common.salesforce.competitor.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.competitor.service.SalesforceCompetitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce/competitor")
public class SalesforceCompetitorController {

    @Autowired
    private SalesforceCompetitorService opportunityCompetitorService;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllOpportunityCompetitors(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {

        try {
            Map<String, Object> result =
                    opportunityCompetitorService.getAllOpportunityCompetitors(accessToken, instanceUrl);

            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Opportunity Competitors fetched successfully",
                    result
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to fetch Opportunity Competitors",
                    null
            );
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunityCompetitorById(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl,
            @PathVariable("id") String competitorId) {

        try {
            Map<String, Object> result =
                    opportunityCompetitorService.getOpportunityCompetitorDetails(competitorId, accessToken, instanceUrl);

            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Opportunity Competitor details fetched successfully",
                    result
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to fetch Opportunity Competitor details",
                    null
            );
        }
    }
}
