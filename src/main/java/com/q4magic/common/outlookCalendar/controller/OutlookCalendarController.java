package com.q4magic.common.outlookCalendar.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.outlookCalendar.service.OutlookCalendarService;
import com.q4magic.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
    @RequestMapping("/outlookCalendar")
public class OutlookCalendarController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OutlookCalendarService outlookCalendarService;

    @GetMapping(value = "/outlookCalendarSignIn")
    public RedirectView outlookCalendarSignIn(HttpServletRequest request) throws Exception {
        return new RedirectView(this.outlookCalendarService.authorize());
    }

    @GetMapping("/oauth")
    public ApiResponse<?> oauth2Callback(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam(value = "code", required = false) String code, @RequestParam(value = "error", required = false) String error) throws Exception {
        Map<String, Object> resBody = new HashMap<>();
        if(error == null) {
            error = "";
        }
        if(error.equals("consent_required")) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Proper Permissions Are Not Provided To Outlook Calendar", resBody);
        } else {
            if (code != null) {
                Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

                this.outlookCalendarService.saveToken(userId, code);
                return new ApiResponse<>(HttpStatus.OK.value(), "Authenticate Successfully.", resBody);
            }
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Authenticate.", resBody);
        }
    }

    @DeleteMapping("/deleteEvent/{eventId}")
    public ApiResponse<?> deleteEvent(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable String eventId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            outlookCalendarService.deleteEvent(userId, eventId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "Event Deleted Successfully.", resBody);
    }

    @GetMapping("/revoke")
    public ApiResponse<?> revoke(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws Exception {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        Map<String, Object> resBody = new HashMap<>();
        resBody = outlookCalendarService.revoke(userId);
        if(resBody.get("error").equals("")) {
            return new ApiResponse<>(HttpStatus.OK.value(), "Disconnected Successfully.", resBody);
        }
        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Constants.ERROR_MSG, resBody);
    }
}
