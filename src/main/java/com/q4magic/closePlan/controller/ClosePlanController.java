package com.q4magic.closePlan.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.closePlan.service.ClosePlanService;
import com.q4magic.common.dto.ClosePlanDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/closeplan")
public class ClosePlanController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ClosePlanService closePlanService;

    @PostMapping("/saveClosePlan")
    public ApiResponse<Map<String, Object>> saveAndGenerateUrl(@RequestBody List<ClosePlanDto> calendarDto, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Close plan created successfully", this.closePlanService.saveAndGenerateUrl(userId, calendarDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create close plan", "");
        }
    }

    @GetMapping("/validateToken")
    public ApiResponse<?> validateToken(@RequestParam String token) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> validateToken = this.closePlanService.validateToken(token);
            if (validateToken != null) {
                return new ApiResponse<>(HttpStatus.OK.value(), validateToken.get("message").toString(), validateToken);
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to validate token", resBody);
        }
    }

    @GetMapping("/changeStatus/{id}")
    public ApiResponse<?> changeStatus(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.closePlanService.changeStatus(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Status changed successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to change status", resBody);
        }
    }

    @GetMapping("/getClosePlanByStatus/{oppId}")
    public ApiResponse<?> getClosePlanByOppIdAndStatus(@PathVariable Integer oppId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Close plan fetched successfully", this.closePlanService.getClosePlanByOppIdAndStatus(oppId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch close plan", resBody);
        }
    }

    @GetMapping("/getClosePlanByOppId/{oppId}")
    public ApiResponse<?> getClosePlanByOppId(@PathVariable Integer oppId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Close plan fetched successfully", this.closePlanService.getClosePlanByOppId(oppId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch close plan", resBody);
        }
    }
}
