package com.q4magic.salesStage.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.salesStage.service.SalesStageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/salesStage")
@Slf4j
@Validated
public class SalesStageController {

    @Autowired
    private SalesStageService salesStageService;

    @GetMapping("/get/all")
    public ApiResponse<?> getAllSalesStages(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Stages fetched successfully", this.salesStageService.getAllSalesStages());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch Stages", resBody);
        }
    }
}
