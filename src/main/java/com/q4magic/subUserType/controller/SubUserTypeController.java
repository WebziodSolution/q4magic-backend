package com.q4magic.subUserType.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.SubUserTypeDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.subUserType.service.SubUserTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subUserType")
public class SubUserTypeController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubUserTypeService userTypeService;

    @GetMapping("/getActions/{subUserTypeId}")
    public ApiResponse<?> getActions(
            @PathVariable Integer subUserTypeId,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Role's policies fetched successfully", this.userTypeService.getPolicy(subUserTypeId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getSubUserTypeById(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Account type fetched successfully", this.userTypeService.getSubUserTypeById(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account types", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllSubUserTypes(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Account type fetched successfully", this.userTypeService.getAllSubUserTypes(userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account types", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createSubUserType(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody SubUserTypeDto subUserTypeDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            subUserTypeDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Account type created successfully", this.userTypeService.createSubUserType(subUserTypeDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @PostMapping("/create/all")
    public ApiResponse<Map<String, Object>> createSubUserTypes(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer userId = Integer.parseInt(requestBody.get("userId").toString());

            List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>) requestBody.get("data");
            for (LinkedHashMap<String, Object> item : data) {
                SubUserTypeDto subUserTypeDto = objectMapper.convertValue(item, SubUserTypeDto.class);
                subUserTypeDto.setCreatedBy(userId);
                this.userTypeService.createSubUserType(subUserTypeDto);
            }

            return new ApiResponse<>(HttpStatus.CREATED.value(), "Account type created successfully", "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateSubUserType(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody SubUserTypeDto subUserTypeDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            subUserTypeDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Account type created successfully", this.userTypeService.updateSubUserType(id, subUserTypeDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteSubUserType(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            this.userTypeService.deleteSubUserType(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Account type deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete account type", "");
        }
    }
}
