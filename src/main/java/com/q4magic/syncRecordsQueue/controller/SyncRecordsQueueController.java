package com.q4magic.syncRecordsQueue.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.syncRecordsQueue.service.SyncRecordsQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/syncRecords")
public class SyncRecordsQueueController {
    @Autowired
    private SyncRecordsQueueService syncRecordsQueueService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getRecords(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Records fetched successfully", this.syncRecordsQueueService.getAllSyncRecordsQueue(userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch records", "");
        }
    }

    @GetMapping("/get/history")
    public ApiResponse<Map<String, Object>> getAllDeleted(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Records fetched successfully", this.syncRecordsQueueService.getAllDeleted(userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch records", "");
        }
    }
}
