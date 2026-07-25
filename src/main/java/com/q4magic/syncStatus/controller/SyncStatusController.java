package com.q4magic.syncStatus.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.SyncStatusDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.salesStage.service.SalesStageService;
import com.q4magic.syncStatus.service.SyncStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/syncStatus")
@Slf4j
@Validated
public class SyncStatusController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private SyncStatusService syncStatusService;

    @GetMapping("/get")
    public ApiResponse<?> getStatus(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Sync status fetched successfully", this.syncStatusService.findByUserId(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch sync status", resBody);
        }
    }

    @GetMapping("/save")
    public ApiResponse<?> saveStatus(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            SyncStatusDto syncStatusDto = new SyncStatusDto();
            syncStatusDto.setCustomerId(userId);
            syncStatusDto.setStatusMessage("Please wait ! We are syncing your data.....");
            this.syncStatusService.saveSyncStatus(syncStatusDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "Sync status fetched successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch sync status", resBody);
        }
    }
}
