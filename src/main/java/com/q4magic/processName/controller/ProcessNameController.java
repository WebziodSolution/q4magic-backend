package com.q4magic.processName.controller;


import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.ProcessNameDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.processName.service.ProcessNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/process-name")
public class ProcessNameController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ProcessNameService processNameService;

    // Get all process names for a specific opportunity
    @GetMapping("/get/all/by-opportunity/{oppId}")
    public ApiResponse<Map<String, Object>> getAllByOpportunity(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer oppId) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Process names fetched successfully",
                    this.processNameService.getAllByOpportunity(oppId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to get process names", "");
        }
    }

    // Get all process names for a specific customer
    @GetMapping("/get/all/by-customer/{customerId}")
    public ApiResponse<Map<String, Object>> getAllByCustomer(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer customerId) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Process names fetched successfully",
                    this.processNameService.getAllByCustomer(customerId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to get process names", "");
        }
    }

    // Get single process name
    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getProcessName(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Process name fetched successfully",
                    this.processNameService.getProcessName(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to get process name", "");
        }
    }

    // Create
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createProcessName(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ProcessNameDto processNameDto) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
                // if you want, auto-set createdBy when not provided
                if (processNameDto.getCreatedBy() == null) {
                    processNameDto.setCreatedBy(userId);
                }
            }

            return new ApiResponse<>(HttpStatus.CREATED.value(),
                    "Process name created successfully",
                    this.processNameService.createProcessName(processNameDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to create process name", "");
        }
    }

    // Update
    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateProcessName(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody ProcessNameDto processNameDto) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Process name updated successfully",
                    this.processNameService.updateProcessName(id, processNameDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to update process name", "");
        }
    }

    // Delete
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteProcessName(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            this.processNameService.deleteProcessName(id);
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Process name deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to delete process name", "");
        }
    }
}
