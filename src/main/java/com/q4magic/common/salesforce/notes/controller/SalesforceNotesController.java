package com.q4magic.common.salesforce.notes.controller;


import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.notes.service.SalesforceNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce/notes")
public class SalesforceNotesController {

    @Autowired
    private SalesforceNotesService salesforceNotesService;

    /**
     * Fetch all Notes and Attachments for a given parent record (e.g. Opportunity, Account)
     */
    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllNotesAndAttachments(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl,
            @RequestParam("parentId") String parentId) {
        try {
            Map<String, Object> result = salesforceNotesService.getAllNotesAndAttachments(accessToken, instanceUrl, parentId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Notes & Attachments fetched successfully", result);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Notes & Attachments", null);
        }
    }
}