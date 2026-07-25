package com.q4magic.performance.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.performance.service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/performance")
@Slf4j
@Validated
public class PerformanceController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/get")
    public ApiResponse<Map<String, Object>> getPerformanceByCustomerId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate,@RequestParam(value = "timeZone", required = false) String timeZone) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Data fetched successfully", this.performanceService.getPerformanceByCustomerId(userId, startDate, endDate));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch data", "");
        }
    }
}
