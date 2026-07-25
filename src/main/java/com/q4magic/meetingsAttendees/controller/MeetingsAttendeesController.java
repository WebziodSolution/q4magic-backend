package com.q4magic.meetingsAttendees.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.MeetingsAttendeesDto;
import com.q4magic.common.dto.MeetingsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.meetings.service.MeetingsService;
import com.q4magic.meetingsAttendees.service.MeetingsAttendeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/meeting-attendees")
public class MeetingsAttendeesController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private MeetingsAttendeesService meetingsAttendeesService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<?> getAllMeetingsAttendeesByMeetingId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Attendees fetched successfully", this.meetingsAttendeesService.findByMeetingsId(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getMeetingsAttendeesById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Attendees fetched successfully", this.meetingsAttendeesService.findById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/saveMeetingAttendees")
    public ApiResponse<?> saveMeetingAttendees(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody MeetingsAttendeesDto meetingsAttendeesDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Attendees added successfully", this.meetingsAttendeesService.addMeetingAttendees(meetingsAttendeesDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PatchMapping("/updateMeetingAttendees/{id}")
    public ApiResponse<?> updateMeetingAttendees(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id, @RequestBody MeetingsAttendeesDto meetingsAttendeesDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Attendees updated successfully", this.meetingsAttendeesService.updateMeetingAttendees(id,meetingsAttendeesDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteMeetingAttendees(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.meetingsAttendeesService.deleteMeetingAttendees(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Attendees deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
