package com.q4magic.timeZones.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.timeZones.service.TimeZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/time-zones")
public class TimeZoneController {

    @Autowired
    private TimeZoneService timeZoneService;

    @GetMapping("/get/all")
    public ApiResponse<?> getTimeZones() {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Time-zones fetched successfully", this.timeZoneService.getTimeZones());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch time-zones", resBody);
        }
    }
}
