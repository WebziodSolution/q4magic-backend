package com.q4magic.opportunityContact.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunityContactDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunityContact.service.OpportunityContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/opportunitiesContact")
public class OpportunityContactController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunityContactService opportunityContactService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getAllOpportunitiesContact(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities contact fetched successfully", this.opportunityContactService.getAllOppContact(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createOpportunitiesContact(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody List<OpportunityContactDto> opportunityContactDtoList) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.opportunityContactService.addOppContact(userId, opportunityContactDtoList, true);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Opportunities contact added successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @PatchMapping("/update")
    public ApiResponse<Map<String, Object>> updateOpportunitiesContact(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody List<OpportunityContactDto> opportunityContactDtoList) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.opportunityContactService.updateOppContact(userId, opportunityContactDtoList, true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunitiesContact(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

            this.opportunityContactService.deleteOppContact(userId,id,true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }
}
