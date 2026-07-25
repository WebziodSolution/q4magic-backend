package com.q4magic.oportunities.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunitiesDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.oportunities.service.OpportunitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/opportunities")
public class OpportunitiesController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunitiesService opportunitiesService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunities(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities details fetched successfully", this.opportunitiesService.getOpportunityById(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities details", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllOpportunities(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "fetchType", required = false) String fetchType,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "salesStage", required = false) List<String> salesStage,
            @RequestParam(value = "status", required = false) List<String> status
    ) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            var result = opportunitiesService.getAllOpportunities(userId, fetchType, search, salesStage, status);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities details fetched successfully", result);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities details", "");
        }
    }

    @GetMapping("/get/allByGroup")
    public ApiResponse<Map<String, Object>> getAllOpportunitiesGroupedByStage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "fetchType", required = false) String fetchType,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "salesStage", required = false) List<String> salesStage,
            @RequestParam(value = "status", required = false) List<String> status
    ) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            var result = opportunitiesService.getAllOpportunitiesGroupedByStage(userId, fetchType, search, salesStage, status);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities details fetched successfully", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities details", "");
        }
    }

    @GetMapping("/get/all/options")
    public ApiResponse<Map<String, Object>> getOpportunityOptions(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam(value = "id", required = false) Integer id) {
        try {
            Integer userId = id != null ? id : jwtUtil.extractUserId(authorizationHeader.substring(7));
            var result = opportunitiesService.getOpportunityOptions(userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities options fetched successfully", result);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities options", "");
        }
    }


    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createOpportunities(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody OpportunitiesDto opportunitiesDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            opportunitiesDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Opportunities created successfully", this.opportunitiesService.createOpportunity(opportunitiesDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create opportunities", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateOpportunities(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody OpportunitiesDto opportunitiesDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            opportunitiesDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities updated successfully", this.opportunitiesService.updateOpportunity(id, opportunitiesDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunities", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunities(@PathVariable Integer id) {
        try {
            this.opportunitiesService.deleteOpportunity(id, true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete opportunities", "");
        }
    }

    @DeleteMapping("/deleteLogo/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunityLogo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.opportunitiesService.deleteOpportunityLogo(id, userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunity logo deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete opportunity logo", "");
        }
    }

    @PatchMapping("/updatedealamount/{id}")
    public ApiResponse<Map<String, Object>> updateOpportunitiesDealAmount(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody Map<Object, String> obj) {
        try {
            this.opportunitiesService.updateOpportunityDeal(id, Integer.parseInt(obj.get("dealAmount").toString()));
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunities", "");
        }
    }

    @PostMapping("/uploadOpportunityLogo")
    public ApiResponse<Map<String, Object>> updateOpportunityLogo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<String, Object> obj) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            String imagePath = obj.get("image").toString();
            Integer oppId = Integer.parseInt(obj.get("oppId").toString());
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunity logo updated successfully", this.opportunitiesService.updateOpportunityLogo(userId, oppId, imagePath));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to upload opportunity logo", "");
        }
    }

    @PostMapping("/updateOpportunityData")
    public ApiResponse<Map<String, Object>> updateOpportunityData(@RequestBody Map<String, Object> obj) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunity updated successfully", this.opportunitiesService.updateOpportunityData(obj));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunity", "");
        }
    }

    @PostMapping("/updateLastOpportunityData")
    public ApiResponse<Map<String, Object>> updateLastOpportunityData(@RequestBody Map<String, Object> obj) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunity updated successfully", this.opportunitiesService.updateLastOpportunityData(obj));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunity", "");
        }
    }

    @GetMapping("/checkOpportunity/{oppId}")
    public ApiResponse<Map<String, Object>> checkOpportunity(@PathVariable Integer oppId) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunity data get successfully", this.opportunitiesService.checkOpportunity(oppId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to get opportunity data", "");
        }
    }

    @PostMapping("/createOpportunityData")
    public ApiResponse<Map<String, Object>> createOpportunityData(@RequestBody Map<String, Object> obj) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Opportunity create successfully", this.opportunitiesService.createOpportunityData(obj));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to created opportunity", "");
        }
    }
}
