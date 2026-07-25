package com.q4magic.authIdDetails.controller;

import com.q4magic.authId.AuthId;
import com.q4magic.authIdDetails.service.AuthIdDetailsService;
import com.q4magic.common.dto.AuthIdDetailsDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authIdDetails")
public class AuthIdDetailsController {
    @Value("${authIdExternalId}")
    private String authIdExternalId;

    @Value("${authIdApiKeyValue}")
    private String authIdApiKeyValue;

    @Value("${authIdAccountNumberStart}")
    private String authIdAccountNumberStart;

    @Autowired
    private AuthIdDetailsService authIdDetailsService;

    @GetMapping("/{email}")
    public ApiResponse<?> getUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable String email) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> authIdDetailsDto = this.authIdDetailsService.getAuthDetails(email);
            return new ApiResponse<>(HttpStatus.OK.value(), "User fetched successfully", authIdDetailsDto);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", resBody);
        }
    }

    @PostMapping("/addUser")
    public ApiResponse<?> addUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody AuthIdDetailsDto authIdDetailsDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> authIdDetailsDto1 = this.authIdDetailsService.createAuthDetails(authIdDetailsDto);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "User added successfully", authIdDetailsDto1);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", resBody);
        }
    }

    @PostMapping("/updateUser")
    public ApiResponse<?> updateUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody AuthIdDetailsDto authIdDetailsDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> authIdDetailsDto1 = this.authIdDetailsService.updateAuthDetails(authIdDetailsDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", authIdDetailsDto1);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", resBody);
        }
    }

    @DeleteMapping("/{authId}")
    public ApiResponse<?> deleteUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable String authId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.authIdDetailsService.deleteAuthDetails(Integer.parseInt(authId));
            return new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", resBody);
        }
    }

    @GetMapping("/login/{email}")
    public ApiResponse<?> login(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable String email) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> res = this.authIdDetailsService.login(email);
            return new ApiResponse<>(HttpStatus.OK.value(), "Login successfully", res);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/deleteUser/{email}")
    public ApiResponse<?> deleteAuthUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable String email) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            AuthId.authIdDeleteAccount(authIdExternalId, authIdApiKeyValue, authIdAccountNumberStart + "-" + email);
            return new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
