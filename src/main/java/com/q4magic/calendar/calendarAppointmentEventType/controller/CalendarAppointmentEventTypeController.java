package com.q4magic.calendar.calendarAppointmentEventType.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.calendar.calendarAppointmentEventType.service.CalendarAppointmentEventTypeService;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.dto.CalendarAppointmentEventTypeDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/calendarAppointmentEventType")
public class CalendarAppointmentEventTypeController {
    @Autowired
    private CalendarAppointmentEventTypeService calendarAppointmentEventTypeService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllEventTypeList(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "id", required = false) Integer id
    ) {
        try {
            Integer userId = null;
            if (id != null) {
                userId = id;
            }
            if (authorizationHeader != null) {
                userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch Event Type Successfully",
                    this.calendarAppointmentEventTypeService.getAllAppointmentEventTypeByUserId(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch event type", "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getEventTypeList(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Integer id
    ) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Event Type fetch successfully",
                    this.calendarAppointmentEventTypeService.getAppointmentEventType(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch event type", "");
        }
    }

    @PostMapping("/saveEventType")
    public ApiResponse<Map<String, Object>> saveEventType(@RequestParam(value = "id", required = false) Integer id, @RequestBody CalendarAppointmentEventTypeDto calendarAppointmentEventTypeDto, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            calendarAppointmentEventTypeDto.setCusId(userId);
            this.calendarAppointmentEventTypeService.saveAppointmentEventType(id, calendarAppointmentEventTypeDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "Event type saved successfully", null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save event type", "");
        }
    }

    @PostMapping("/deleteEventType/{id}")
    public ApiResponse<?> deleteEventType(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.calendarAppointmentEventTypeService.deleteAppointmentEventType(id);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "Event type deleted successfully.", resBody);
    }
}
