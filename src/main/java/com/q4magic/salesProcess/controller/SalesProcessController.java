package com.q4magic.salesProcess.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.SalesProcessDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.salesProcess.service.SalesProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sales-process")
public class SalesProcessController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private SalesProcessService saleProcessService;

    // Get all steps for one opportunity (Decision Map)
    @GetMapping("/get/all/{oppId}")
    public ApiResponse<Map<String, Object>> getAllByOpportunity(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer oppId) {

        try {
            // just validate token if needed
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Sale process fetched successfully",
                    this.saleProcessService.getAllByOpportunity(oppId));
        } catch (Exception e) {
            return new ApiResponse<>(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to get sale process",
                    ""
            );
        }
    }

    // Get single step
    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getSaleProcess(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id) {

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Sale process fetched successfully",
                    this.saleProcessService.getSaleProcess(id));
        } catch (Exception e) {
            return new ApiResponse<>(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to get sale process",
                    ""
            );
        }
    }

    // Create new step
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createSaleProcess(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody SalesProcessDto saleProcessDto) {

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.CREATED.value(),
                    "Sale process created successfully",
                    this.saleProcessService.createSaleProcess(saleProcessDto));
        } catch (Exception e) {
            return new ApiResponse<>(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to create sale process",
                    ""
            );
        }
    }

    // Update step
    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateSaleProcess(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id,
            @RequestBody SalesProcessDto saleProcessDto) {

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Sale process updated successfully",
                    this.saleProcessService.updateSaleProcess(id, saleProcessDto));
        } catch (Exception e) {
            return new ApiResponse<>(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to update sale process",
                    ""
            );
        }
    }

    // Delete step
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteSaleProcess(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id) {

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            this.saleProcessService.deleteSaleProcess(id);
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Sale process deleted successfully",
                    "");
        } catch (Exception e) {
            return new ApiResponse<>(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Fail to delete sale process",
                    ""
            );
        }
    }
}
