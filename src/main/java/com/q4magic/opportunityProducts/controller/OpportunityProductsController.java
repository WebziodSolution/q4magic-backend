package com.q4magic.opportunityProducts.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunityProductsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunityProducts.service.OpportunityProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opportunitiesProduct")
public class OpportunityProductsController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunityProductsService opportunityProductsService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunitiesProduct(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities product fetched successfully", this.opportunityProductsService.getOppProducts(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities product", "");
        }
    }

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getAllOpportunitiesProduct(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities product fetched successfully", this.opportunityProductsService.getAllOppProducts(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch opportunities product", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createOpportunitiesProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody OpportunityProductsDto opportunityProductsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Opportunities product created successfully", this.opportunityProductsService.createOppProducts(opportunityProductsDto, true, userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create opportunities product", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateOpportunitiesProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id, @RequestBody OpportunityProductsDto opportunityProductsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities product update successfully", this.opportunityProductsService.updateOppProducts(id, opportunityProductsDto, true, userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update opportunities product", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunitiesProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.opportunityProductsService.deleteOppProducts(id, true, userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities product delete successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete opportunities product", "");
        }
    }
}
