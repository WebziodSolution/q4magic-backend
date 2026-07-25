package com.q4magic.opportunityPartnerDetails.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunityPartnerDetailsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunityPartnerDetails.service.OpportunityPartnerDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opportunityPartner")
@Slf4j
@Validated
public class OpportunityPartnerDetailsController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunityPartnerDetailsService opportunityPartnerDetailsService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getAllOpportunitiesPartner(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities partner details fetched successfully", this.opportunityPartnerDetailsService.getAllOpportunityPartnerDetails(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities partner details", "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunitiesPartner(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "OpportunitiesPartner details fetched successfully", this.opportunityPartnerDetailsService.getOpportunityPartnerDetailsById(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities partner details", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createOpportunitiesPartner(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody OpportunityPartnerDetailsDto opportunityPartnerDetailsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            opportunityPartnerDetailsDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "OpportunitiesPartner details created successfully", this.opportunityPartnerDetailsService.createOpportunityPartnerDetails(opportunityPartnerDetailsDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create opportunities partner details", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateOpportunitiesPartner(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody OpportunityPartnerDetailsDto opportunityPartnerDetailsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            opportunityPartnerDetailsDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "OpportunitiesPartner details updated successfully", this.opportunityPartnerDetailsService.updateOpportunityPartnerDetails(id, opportunityPartnerDetailsDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunities partner details", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunitiesPartner(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.opportunityPartnerDetailsService.deleteOpportunityPartnerDetails(id, true, userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "OpportunitiesPartner details delete successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete opportunities partner details", "");
        }
    }
}
