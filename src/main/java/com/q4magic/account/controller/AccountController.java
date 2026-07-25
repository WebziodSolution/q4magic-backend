package com.q4magic.account.controller;

import com.q4magic.account.service.AccountService;
import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.AccountDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private AccountService accountService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getAccount(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer accountId) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Account details fetched successfully", this.accountService.getAccountById(accountId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account details", "");
        }
    }

    @GetMapping("/getall")
    public ApiResponse<Map<String, Object>> getAllAccount(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,@RequestParam (value = "fetchType", required = false) String fetchType) {
        try {
            Integer accountId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Account details fetched successfully", this.accountService.getAllAccounts(accountId, fetchType));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch account details", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createAccount(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,@RequestBody AccountDto accountDto) {
        try {
            Integer accountId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            accountDto.setCreatedBy(accountId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Account created successfully", this.accountService.createAccount(accountDto, true,true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create account", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateAccount(@PathVariable("id") Integer id, @RequestBody AccountDto accountDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Account details updated successfully", this.accountService.updateAccount(id, accountDto, true,true));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update account details", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteAccount(@PathVariable("id") Integer id) {
        try {
            this.accountService.deleteAccount(id, true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Account deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to deleted account", "");
        }
    }
}
