package com.q4magic.meetingSummary.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.meetingSummary.service.MeetingSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/meetingSummary")
public class MeetingSummaryController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private MeetingSummaryService meetingSummaryService;

    @GetMapping("/getByOppId/{id}")
    public ApiResponse<Map<String, Object>> getMeetingSummaryByOppId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Note fetched successfully", this.meetingSummaryService.getMeetingSummaryByOppId(id, userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch note", "");
        }
    }
}
