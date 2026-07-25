package com.q4magic.common.salesforce.account.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.account.service.SalesforceAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/salesforce")
public class SalesforceAccountController {

    @Autowired
    private SalesforceAccountService salesforceAccountService;

    @GetMapping("/account/get/{id}")
    public ApiResponse<Map<String, Object>> getAccount(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl,
            @PathVariable("id") String accountId) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Account details fetched successfully", this.salesforceAccountService.getAccountDetails(accountId, accessToken, instanceUrl));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account details", "");
        }
    }

    @GetMapping("/account/getall")
    public ApiResponse<Map<String, Object>> getAllAccount(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Account details fetched successfully", this.salesforceAccountService.getAllAccounts(accessToken, instanceUrl));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account details", "");
        }
    }
}
