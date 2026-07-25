package com.q4magic.opportunitiesCurrentEnvironment.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunitiesCurrentEnvironmentDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunitiesCurrentEnvironment.service.OpportunitiesCurrentEnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opportunitiesCurrentEnvironment")
public class OpportunitiesCurrentEnvironmentController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunitiesCurrentEnvironmentService opportunitiesCurrentEnvironmentService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunitiesCurrentEnvironment(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities current env details fetched successfully", this.opportunitiesCurrentEnvironmentService.getOpportunitiesCurrentEnvironment(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch current env", "");
        }
    }

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getOpportunitiesCurrentEnvironmentByOppId(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities current env details fetched successfully", this.opportunitiesCurrentEnvironmentService.getOpportunitiesCurrentEnvironmentByOppId(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch current env", "");
        }
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> addOpportunitiesCurrentEnvironment(@RequestBody OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Opportunities current env details saved successfully", this.opportunitiesCurrentEnvironmentService.addOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironmentDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to add current env", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateOpportunitiesCurrentEnvironment(@PathVariable("id") Integer id, @RequestBody OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities current env details saved successfully", this.opportunitiesCurrentEnvironmentService.updateOpportunitiesCurrentEnvironment(id, opportunitiesCurrentEnvironmentDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update current env", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunitiesCurrentEnvironment(@PathVariable("id") Integer id) {
        try {
            this.opportunitiesCurrentEnvironmentService.deleteOpportunitiesCurrentEnvironment(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities current env details saved successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete current env", "");
        }
    }
}
