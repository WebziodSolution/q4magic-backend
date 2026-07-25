package com.q4magic.common.salesforce.activityHistory.controller;


import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.activityHistory.service.SalesforceActivityHistoryService;
import com.q4magic.common.salesforce.openActivity.service.SalesforceOpenActivitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce/activities")
public class SalesforceActivitiesController {

    @Autowired
    private SalesforceOpenActivitiesService openActivitiesService;

    @Autowired
    private SalesforceActivityHistoryService activityHistoryService;


    /**
     * Fetch all Open Activities from Salesforce
     */
    @GetMapping("/open/getAll")
    public ApiResponse<Map<String, Object>> getAllOpenActivities(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        try {
            Map<String, Object> data = openActivitiesService.getAllOpenActivities(accessToken, instanceUrl);
            return new ApiResponse<>(HttpStatus.OK.value(), "Open Activities fetched successfully", data);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Open Activities", e.getMessage());
        }
    }

    @GetMapping("/open/get/{id}")
    public ApiResponse<Map<String, Object>> getOpenActivities(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl,
            @PathVariable("id") String id
    ) {
        try {
            Map<String, Object> data = openActivitiesService.getOpenActivityById(accessToken, instanceUrl, id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Open Activities fetched successfully", data);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Open Activities", e.getMessage());
        }
    }

    /**
     * Fetch all Activity History from Salesforce
     */
    @GetMapping("/history/getAll")
    public ApiResponse<Map<String, Object>> getAllActivityHistory(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        try {
            Map<String, Object> data = activityHistoryService.getAllActivityHistory(accessToken, instanceUrl);
            return new ApiResponse<>(HttpStatus.OK.value(), "Activity History fetched successfully", data);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Activity History", e.getMessage());
        }
    }

    @GetMapping("/history/get/{id}")
    public ApiResponse<Map<String, Object>> getActivityHistory(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl,
            @PathVariable("id") String id
    ) {
        try {
            Map<String, Object> data = activityHistoryService.getActivityHistoryById(accessToken, instanceUrl, id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Activity History fetched successfully", data);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Activity History", e.getMessage());
        }
    }
}