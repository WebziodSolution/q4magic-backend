package com.q4magic.common.googleCalendar.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.dto.googleCalendar.GoogleAuthUrlResponse;
import com.q4magic.common.dto.googleCalendar.GoogleCalendarEventDto;
import com.q4magic.common.googleCalendar.service.GoogleCalendarService;
import com.q4magic.common.models.Customers;
import com.q4magic.common.outlookCalendar.service.OutlookCalendarService;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/google-calendar")
public class GoogleCalendarController {

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OutlookCalendarService outlookCalendarService;

    @GetMapping("/getCalendarAuthentication")
    public ApiResponse<?> getCalendarAuthentication(
            @RequestHeader(value = "Authorization") String authorizationHeader) {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        Customers customer = customersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + userId));

        Map<String, Object> resBody = new HashMap<>();

        boolean googleCalendar = false;
        String googleCalendarEmail = "";

        if (customer.getGoogleCalendarRefreshToken() != null && !customer.getGoogleCalendarRefreshToken().trim().isEmpty()
                && customer.getGoogleCalendarAccessToken() != null && !customer.getGoogleCalendarAccessToken().trim().isEmpty()) {
            try {
                String newAccessToken = googleCalendarService.refreshToken(customer.getGoogleCalendarRefreshToken());
                if (newAccessToken != null && !newAccessToken.trim().isEmpty()) {
                    customer.setGoogleCalendarAccessToken(newAccessToken);
                    customersRepository.save(customer);
                    googleCalendar = true;
                    if (customer.getGoogleCalendarEmail() != null && !customer.getGoogleCalendarEmail().trim().isEmpty()) {
                        googleCalendarEmail = customer.getGoogleCalendarEmail();
                    }
                } else {
                    customer.setGoogleCalendarAccessToken(null);
                    customer.setGoogleCalendarRefreshToken(null);
                    customer.setGoogleCalendarEmail(null);
                    customer.setGoogleCalendarSyncTime(null);
                    customersRepository.save(customer);
                }
            } catch (Exception e) {
                customer.setGoogleCalendarAccessToken(null);
                customer.setGoogleCalendarRefreshToken(null);
                customer.setGoogleCalendarEmail(null);
                customer.setGoogleCalendarSyncTime(null);
                customersRepository.save(customer);
            }
        } else {
            if (customer.getGoogleCalendarAccessToken() != null || customer.getGoogleCalendarRefreshToken() != null
                    || customer.getGoogleCalendarEmail() != null || customer.getGoogleCalendarSyncTime() != null) {
                customer.setGoogleCalendarAccessToken(null);
                customer.setGoogleCalendarRefreshToken(null);
                customer.setGoogleCalendarEmail(null);
                customer.setGoogleCalendarSyncTime(null);
                customersRepository.save(customer);
            }
        }

        boolean outlookCalendar = false;
        String outlookCalendarEmail = "";

        if (customer.getOutlookCalendarRefreshToken() != null && !customer.getOutlookCalendarRefreshToken().trim().isEmpty()
                && customer.getOutlookCalendarAccessToken() != null && !customer.getOutlookCalendarAccessToken().trim().isEmpty()) {
            try {
                String newAccessToken = outlookCalendarService.refreshToken(customer.getOutlookCalendarRefreshToken(), customer);
                if (newAccessToken != null && !newAccessToken.trim().isEmpty()) {
                    outlookCalendar = true;
                    if (customer.getOutlookCalendarEmail() != null && !customer.getOutlookCalendarEmail().trim().isEmpty()) {
                        outlookCalendarEmail = customer.getOutlookCalendarEmail();
                    }
                } else {
                    customer.setOutlookCalendarAccessToken(null);
                    customer.setOutlookCalendarRefreshToken(null);
                    customer.setOutlookCalendarEmail(null);
                    customer.setOutlookCalendarSyncTime(null);
                    customersRepository.save(customer);
                }
            } catch (Exception e) {
                customer.setOutlookCalendarAccessToken(null);
                customer.setOutlookCalendarRefreshToken(null);
                customer.setOutlookCalendarEmail(null);
                customer.setOutlookCalendarSyncTime(null);
                customersRepository.save(customer);
            }
        } else {
            if (customer.getOutlookCalendarAccessToken() != null || customer.getOutlookCalendarRefreshToken() != null
                    || customer.getOutlookCalendarEmail() != null || customer.getOutlookCalendarSyncTime() != null) {
                customer.setOutlookCalendarAccessToken(null);
                customer.setOutlookCalendarRefreshToken(null);
                customer.setOutlookCalendarEmail(null);
                customer.setOutlookCalendarSyncTime(null);
                customersRepository.save(customer);
            }
        }

        resBody.put("googleCalendar", googleCalendar);
        resBody.put("outlookCalendar", outlookCalendar);
        resBody.put("googleCalendarEmail", googleCalendarEmail);
        resBody.put("outlookCalendarEmail", outlookCalendarEmail);

        return new ApiResponse<>(HttpStatus.OK.value(), "Fetch Calendar Authentication Successfully.", resBody);
    }

    // ======================================================================================
    // 1) GET AUTH URL (Generate Google OAuth URL)
    // ======================================================================================
    @GetMapping("/auth-url")
    public ApiResponse<Map<String, Object>> getAuthUrl(@RequestParam("customerId") Integer customerId) {
        try {
            GoogleAuthUrlResponse response = googleCalendarService.getAuthorizationUrl(customerId);

            Map<String, Object> data = new HashMap<>();
            data.put("url", response.getUrl());

            return new ApiResponse<>(HttpStatus.OK.value(), "Google auth URL generated", data);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to generate Google Auth URL", null);
        }
    }

    // ======================================================================================
    // 2) OAUTH CALLBACK (Google returns code here)
    // ======================================================================================
    @GetMapping("/oauth2/callback")
    public ApiResponse<String> oauth2Callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        try {
            googleCalendarService.handleOAuthCallback(code, state);
            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Google Calendar connected successfully", "connected");

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to connect Google Calendar", e.getMessage());
        }
    }

    // ======================================================================================
    // 3) LIST EVENTS
    // ======================================================================================
    @GetMapping("/events")
    public ApiResponse<List<GoogleCalendarEventDto>> listEvents(
            @RequestParam("timeMin") String timeMinIso,
            @RequestParam("timeMax") String timeMaxIso,
            @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

            Instant timeMin = Instant.parse(timeMinIso);
            Instant timeMax = Instant.parse(timeMaxIso);

            List<GoogleCalendarEventDto> events = googleCalendarService.listEvents(userId, timeMin, timeMax);

            return new ApiResponse<>(HttpStatus.OK.value(),
                    "Google Calendar events fetched successfully", events);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to fetch Google Calendar events", null);
        }
    }

    @GetMapping("/revoke")
    public ApiResponse<?> revoke(@RequestHeader(value = "Authorization", required = false) String authorizationHeader)
            throws Exception {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        Map<String, Object> resBody = new HashMap<>();
        resBody = googleCalendarService.revoke(userId);
        if (resBody.get("error").equals("")) {
            return new ApiResponse<>(HttpStatus.OK.value(), "Disconnected Successfully.", resBody);
        }
        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
    }
}
