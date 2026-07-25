package com.q4magic.emailScrapingRequest.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.EmailScrapingRequestsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.emailScrapingRequest.service.EmailScrapingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/emailScrapingRequest")
public class EmailScrapingRequestController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private EmailScrapingRequestService emailScrapingRequestService;

    @GetMapping("/get/all")
    public ApiResponse<?> getAll(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Email scraping requests fetched successfully", this.emailScrapingRequestService.getAllEmailScrapingRequests(loginUserId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Email scraping requests updated successfully", this.emailScrapingRequestService.getEmailScrapingRequests(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/changeStatus/{id}")
    public ApiResponse<?> changeStatus(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.emailScrapingRequestService.changeScrapingRequestStatus(id, 0);
            return new ApiResponse<>(HttpStatus.OK.value(), "Email scraping requests updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createEmailRequest(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody EmailScrapingRequestsDto emailScrapingRequestsDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            emailScrapingRequestsDto.setCreatedBy(loginUserId);
            this.emailScrapingRequestService.createEmailScrapingRequest(emailScrapingRequestsDto);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Email scraping request created successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/changeScrapingMessageCount/{id}/{count}")
    public ApiResponse<?> changeScrapingRequestMessageCount(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @PathVariable Integer count) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.emailScrapingRequestService.changeScrapingRequestMessageCount(id, count);
            return new ApiResponse<>(HttpStatus.OK.value(), "Email scraping requests updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
