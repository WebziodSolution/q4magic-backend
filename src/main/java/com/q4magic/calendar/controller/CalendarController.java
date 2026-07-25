package com.q4magic.calendar.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.calendar.service.CalendarService;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.dto.CalendarDto;
import com.q4magic.common.dto.DeleteEventDto;
import com.q4magic.common.googleCalendar.service.GoogleCalendarService;
import com.q4magic.common.googleCalendar.serviceImpl.GoogleCalendarServiceImpl;
import com.q4magic.common.models.CalendarDetails;
import com.q4magic.common.repository.CalendarDetailsRepository;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    @Autowired
    private CalendarDetailsRepository calendarDetailsRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getEventList(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(value = "timeZone") String timeZone
    ) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Event fetch successfully",
                    this.calendarService.getEventList(userId, start, end, timeZone));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch event", "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getEventList(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "timeZone") String timeZone,
            @PathVariable Integer id
    ) {
        try {
            if (id == null || id <= 0) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid event id", Map.of());
            }
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Event fetch successfully",
                    this.calendarService.getEvent(userId, id, timeZone));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch event", "");
        }
    }

    @GetMapping("/getSync")
    public ApiResponse<?> getSync(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @Param("timeZone") String timeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            resBody = calendarService.getSync(userId, timeZone);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to sync events", resBody);
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "Sync Event Successfully.", resBody);
    }

    @PostMapping("/saveEvent")
    public ApiResponse<Map<String, Object>> saveEvent(@RequestBody CalendarDto calendarDto, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), calendarDto.getId() == null ? "Event added successfully" : "Event updated successfully", this.calendarService.saveEvent(userId, calendarDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save event", "");
        }
    }

    @PostMapping("/deleteEvent")
    public ApiResponse<?> deleteEvent(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,@RequestBody DeleteEventDto deleteEventDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            calendarService.deleteEvent(userId, deleteEventDto);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "Event Deleted Successfully.", resBody);
    }

    @DeleteMapping("/delete/all/google")
    public ApiResponse<?> deleteAllGoogleEvents(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            List<CalendarDetails> calendarDetails = this.calendarDetailsRepository.findAll();
            if (!calendarDetails.isEmpty()) {
                for (CalendarDetails calendarDetail : calendarDetails) {
                    this.googleCalendarService.deleteEvent(userId,calendarDetail.getCaldSycId());
                }
            }
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "All Event Deleted Successfully.", resBody);
    }
}
