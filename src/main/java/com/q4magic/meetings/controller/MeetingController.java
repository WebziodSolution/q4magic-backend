package com.q4magic.meetings.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.MeetingsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.meetings.service.MeetingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/meeting")
public class MeetingController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private MeetingsService meetingsService;

    @GetMapping("/getAllByOppId")
    public ApiResponse<?> getAllMeetingsByOppId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam("id") Integer id, @RequestParam("timeZone") String timeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Meetings fetched successfully", this.meetingsService.getAllMeetingsByOppId(id, timeZone));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/saveMeeting")
    public ApiResponse<?> createMeeting(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody MeetingsDto meetingsDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.meetingsService.createMeeting(meetingsDto);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Meetings created successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteMeeting(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.meetingsService.deleteMeeting(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Meetings deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/deleteByCalendar/{id}")
    public ApiResponse<?> deleteMeetingByCalendar(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.meetingsService.deleteMeetingByCalendar(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Meetings deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
