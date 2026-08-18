package com.q4magic.customers.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.*;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.AccountRepository;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.customers.service.CustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomersController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private CustomersService customersService;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/getAllSubUsers")
    public ApiResponse<?> getAllSubUsers(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch customers successfully", this.customersService.getAllSubUsers(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch customers", resBody);
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAllCustomers() {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch customers successfully", this.customersService.getAllCustomers());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch customers", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getCustomerById(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch customer successfully", this.customersService.getCustomerById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getReportHierarch/{id}")
    public ApiResponse<?> getReportHierarch(@PathVariable("id") Integer customerId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Report Hierarch fetched successfully", this.customersService.reportHierarch(customerId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getCustomerByEmail/{email}")
    public ApiResponse<?> getCustomerByEmail(@PathVariable String email) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch customer successfully", this.customersService.getCustomerByEmail(email));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getUserSalesForceToken/{id}")
    public ApiResponse<?> getUserSalesForceToken(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch customer data successfully", this.customersService.getUserSalesForceToken(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/removeUserSalesForceToken/{id}")
    public ApiResponse<?> removeUserSalesForceToken(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Customers customers = this.customersRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
            customers.setSalesforceInstanceUrl(null);
            customers.setSalesforceAccessToken(null);
            this.customersRepository.save(customers);
            return new ApiResponse<>(HttpStatus.OK.value(), "Customer data deleted successfully", null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createCustomer(@RequestBody CustomersDto customersDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Customer added successfully", this.customersService.createCustomer(customersDto, "owner", false));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/create/subuser")
    public ApiResponse<?> createSubUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody CustomersDto customersDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            customersDto.setParentUserId(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Sub-user added successfully", this.customersService.createCustomer(customersDto, "Subuser", true));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateCustomer(@PathVariable Integer id, @RequestBody CustomersDto customersDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Customer updated successfully", this.customersService.updateCustomer(id, customersDto, "owner"));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PatchMapping("/update/subuser/{id}")
    public ApiResponse<?> updateSubUser(@PathVariable Integer id, @RequestBody CustomersDto customersDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Sub-user updated successfully", this.customersService.updateCustomer(id, customersDto, "Subuser"));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteCustomer(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.customersService.deleteCustomer(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Customer deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete customer", resBody);
        }
    }

    @GetMapping("/verifyEmail")
    public ApiResponse<?> isEmailExits(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String email,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id
    ) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = null;
            if (id != null && !"null".equalsIgnoreCase(id)) {
                userId = Integer.parseInt(id);
            } else if ((id == null || "null".equalsIgnoreCase(id)) && "subuser".equals(type)) {
                userId = null;
            } else if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            Map<String, Object> isEmailExits = this.customersService.isEmailExits(email, userId);
            if (isEmailExits.containsKey("ZeroBounce")) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), isEmailExits.get("ZeroBounce").toString(), "");
            }
            if (isEmailExits.containsKey("isEmailExits")) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), isEmailExits.get("isEmailExits").toString(), "");
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Email verification successful", "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/verifyUsername")
    public ApiResponse<?> isUserNameExits(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String username) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            Map<String, Object> isEmailExits = this.customersService.isUserNameExits(username, userId);
            if (isEmailExits.containsKey("isUserNameExits")) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), isEmailExits.get("isUserNameExits").toString(), "");
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Username verification successful", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/login")
    public ApiResponse<?> userLogin(@RequestBody LoginDto loginDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            resBody = this.customersService.userLogin(loginDto);
            if (resBody.containsKey("error")) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), resBody.get("error").toString(), resBody);
            }
            if (resBody.containsKey("loginPreference")) {
                return new ApiResponse<>(HttpStatus.OK.value(), "Login successful", resBody);
            }
            if (resBody.isEmpty()) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid credentials", resBody);
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Login successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/forgotpassword")
    public ApiResponse<?> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            resBody = this.customersService.forgotPassword(forgotPasswordDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "Forgot password successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/resetpassword")
    public ApiResponse<?> resetPassword(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ResetPasswordDto resetPasswordDto) {

        try {
            Map<String, Object> response = this.customersService.resetPassword(resetPasswordDto);

            if (response.containsKey("success")) {
                return new ApiResponse<>(HttpStatus.OK.value(), "Password reset successfully", response);
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to reset password", response);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error resetting password", Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validateToken")
    public ApiResponse<?> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String token) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> validateToken = this.customersService.validateToken(token);
            if (validateToken != null) {
                return new ApiResponse<>(HttpStatus.OK.value(), validateToken.get("message").toString(), validateToken);
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch user details", resBody);
        }
    }

    @GetMapping("/validateSubUserToken")
    public ApiResponse<?> validateSubUserToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String token) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            Map<String, Object> validateToken = this.customersService.validateSubUserToken(token);
            if (validateToken != null) {
                return new ApiResponse<>(HttpStatus.OK.value(), validateToken.get("message").toString(), validateToken);
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid token", resBody);
        }
    }


    @PostMapping("/get/dashboard")
    public ApiResponse<?> getCustomerById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody CustomerDashboardRequestDto customerDashboardRequestDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch data successfully", this.customersService.getDashboardData(userId, customerDashboardRequestDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/changePassword")
    public ApiResponse<?> changePassword(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<String, Object> data) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            data.put("userId", userId);
            resBody = this.customersService.changePassword(data);
            if (resBody.containsKey("error")) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), resBody.get("error").toString(), resBody);
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Password change successfully", resBody);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/sendRegisterInvitation")
    public ApiResponse<?> sendRegisterInvitation(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<String, Object> data) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            if (this.customersService.sendRegisterInvitation(data)) {
                return new ApiResponse<>(HttpStatus.OK.value(), "Invitation sent successfully", "");
            } else {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Fail to send invitation", "");
            }
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/saveTimeZone")
    public ApiResponse<?> saveTimeZone(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam(value = "timeZone") String timeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.customersService.saveCustomerTimeZone(timeZone, userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Save timezone successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/saveWebConference")
    public ApiResponse<?> saveWebConference(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<Object, String> webConference) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.customersService.saveWebConference(webConference.get("webConference"), userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "WebConference save successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/saveMailNotification")
    public ApiResponse<?> saveMailNotification(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<Object, String> emailNotification) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.customersService.saveMailNotification(emailNotification.get("emailNotification"), userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "EmailNotification save successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/saveDefaultCalendar")
    public ApiResponse<?> saveDefaultCalendar(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<Object, String> defaultCalendar) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.customersService.saveDefaultCalendar(defaultCalendar.get("defaultCalendar"), userId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Default calendar save successful", resBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/meetingquota")
    public ApiResponse<?> meetingQuota(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String quota) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            if (userId != null) {
                Customers customers = this.customersRepository.findById(userId).orElse(null);
                if (customers != null) {
                    customers.setMeetingQuota(quota);
                    this.customersRepository.save(customers);
                    return new ApiResponse<>(HttpStatus.OK.value(), "Meeting quota set successful", "");
                } else {
                    return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", "");
                }
            }
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "User id required", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getAllSubUsersWithParntSubUser")
    public ApiResponse<Map<String, Object>> getAllSubUsersWithParntSubUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch sub-users successfully", this.customersService.getAllSubUsersWithParntSubUser(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch sub-users", "");
        }
    }
}
