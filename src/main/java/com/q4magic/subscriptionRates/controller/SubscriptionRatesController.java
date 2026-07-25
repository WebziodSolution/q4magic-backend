package com.q4magic.subscriptionRates.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.subscriptionRates.service.SubscriptionRatesService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/subscriptionRates")
@Slf4j
@Validated
public class SubscriptionRatesController {

    @Autowired
    private SubscriptionRatesService subscriptionRatesService;

    @GetMapping("/get/all")
    public ApiResponse<?> getAllSubscriptionRates() {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Rates fetched successfully", this.subscriptionRatesService.getAllSubscriptionRates());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to rates", resBody);
        }
    }
}
