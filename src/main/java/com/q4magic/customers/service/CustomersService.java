package com.q4magic.customers.service;

import com.q4magic.common.dto.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

public interface CustomersService extends UserDetailsService {
    Map<String,Object> getUserSalesForceToken(Integer id);

    CustomerDashboardDto getDashboardData(Integer id,CustomerDashboardRequestDto customerDashboardRequestDto);

    List<CustomersDto> getAllSubUsers(Integer id);

    List<CustomersDto> getAllSubUsersWithParntSubUser(Integer userId);

    List<CustomersDto> getAllCustomers();

    CustomersDto getCustomerById(Integer id);

    CustomersDto getCustomerByEmail(String email);

    CustomersDto createCustomer(CustomersDto customersDto, String type, Boolean createAccount);

    CustomersDto updateCustomer(Integer id, CustomersDto customersDto, String type);

    void deleteCustomer(Integer id);

    Map<String, Object> isEmailExits(String email, Integer userId);

    Map<String, Object> isUserNameExits(String userName, Integer userId);

    Map<String, Object> userLogin(LoginDto loginDto);

    Map<String, Object> forgotPassword(ForgotPasswordDto forgotPasswordDto);

    Map<String, Object> resetPassword(ResetPasswordDto resetPasswordDto);

    Map<String, Object> validateToken(String token);

    Map<String, Object> changePassword(Map<String, Object> data);

    boolean sendRegisterInvitation(Map<String, Object> data);

    Map<String, Object> validateSubUserToken(String token);

    void saveCustomerTimeZone(String timeZone,Integer customerId);

    void saveWebConference(String webConference,Integer customerId);

    void saveMailNotification(String mailNotification,Integer customerId);

    void saveDefaultCalendar(String defaultCalendar,Integer customerId);

    Map<String, Object> reportHierarch(Integer customerId);

}
