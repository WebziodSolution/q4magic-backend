package com.q4magic.payment.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.payment.PaymentGatewayRequestDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.payment.PaymentGatewayConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateCustomerProfileController;
import net.authorize.api.controller.CreateCustomerPaymentProfileController;
import net.authorize.api.controller.DeleteCustomerProfileController;
import net.authorize.api.controller.GetCustomerProfileController;
import net.authorize.api.controller.UpdateCustomerPaymentProfileController;
import net.authorize.api.controller.base.ApiOperationBase;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/paymentGateway")
@Slf4j
@Validated
public class PaymentController {
    @Value("${companyName}")
    String companyName;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private PaymentGatewayConfiguration paymentGatewayConfiguration;

    @Autowired
    private CustomersRepository customersRepository;

    @PostMapping("/createPaymentProfile")
    public ApiResponse<?> createPaymentProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody @Valid PaymentGatewayRequestDto paymentGatewayRequestDto) {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        String[] cardExp = paymentGatewayRequestDto.getExpMonthYear().split("/");

        int expYear = Integer.parseInt(cardExp[1]);
        int expMonth = Integer.parseInt(cardExp[0]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR) % 100;
        int month = calendar.get(Calendar.MONTH);

        if (year >= expYear) {
            if (expMonth <= (month + 1)) {
                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Please Confirm The Expiration Date Of Your Credit Card Is Correct And Matches The Format Of MM/YYYY", "Error");
            }
        }

        // Set the request to operate in either the sandbox or production environment
        ApiOperationBase.setEnvironment(paymentGatewayConfiguration.environmentModeCheck());

        // Create object with merchant authentication details
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(paymentGatewayConfiguration.authorizenetLonginId());
        merchantAuthenticationType.setTransactionKey(paymentGatewayConfiguration.authorizenetTransactionKey());

        // Populate the payment data
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(paymentGatewayRequestDto.getCardNumber());
        creditCard.setExpirationDate(cardExp[1] + "-" + cardExp[0]);
        creditCard.setCardCode(paymentGatewayRequestDto.getCardCode());
        PaymentType paymentType = new PaymentType();
        paymentType.setCreditCard(creditCard);

        // Set Me
        CustomerAddressType customerAddressType = new CustomerAddressType();
        customerAddressType.setFirstName(paymentGatewayRequestDto.getName());
//        customerAddressType.setLastName(paymentGatewayRequestDto.getLastName());
        customerAddressType.setEmail(paymentGatewayRequestDto.getEmail());
        customerAddressType.setCompany(paymentGatewayRequestDto.getBusinessName());
        customerAddressType.setAddress(paymentGatewayRequestDto.getAddress());
        customerAddressType.setCity(paymentGatewayRequestDto.getCity());
        customerAddressType.setState(paymentGatewayRequestDto.getState());
        customerAddressType.setCountry(paymentGatewayRequestDto.getCountry());
        customerAddressType.setPhoneNumber(paymentGatewayRequestDto.getPhone());
        customerAddressType.setZip(paymentGatewayRequestDto.getPostCode());

        // Set payment profile data
        CustomerPaymentProfileType customerPaymentProfileType = new CustomerPaymentProfileType();
        customerPaymentProfileType.setCustomerType(CustomerTypeEnum.INDIVIDUAL);
        customerPaymentProfileType.setPayment(paymentType);
        customerPaymentProfileType.setBillTo(customerAddressType);

        // Set customer profile data
        CustomerProfileType customerProfileType = new CustomerProfileType();
        customerProfileType.setEmail(paymentGatewayRequestDto.getEmail());
        customerProfileType.getPaymentProfiles().add(customerPaymentProfileType);
        customerProfileType.setMerchantCustomerId(paymentGatewayConfiguration.getEnvsys().toUpperCase() + "-" + companyName + "-" + userId);
        customerProfileType.setDescription(paymentGatewayRequestDto.getName());

        // Create the API request and set the parameters for this specific request
        CreateCustomerProfileRequest apiRequest = new CreateCustomerProfileRequest();
        apiRequest.setMerchantAuthentication(merchantAuthenticationType);
        apiRequest.setProfile(customerProfileType);
        apiRequest.setValidationMode(paymentGatewayConfiguration.validationModeEnumCheck());

        // Call the controller
        CreateCustomerProfileController controller = new CreateCustomerProfileController(apiRequest);
        controller.execute();

        // Get the response
        CreateCustomerProfileResponse response = new CreateCustomerProfileResponse();
        response = controller.getApiResponse();

        // Parse the response to determine results
        if (response != null) {
            // If API Response is OK, go ahead and check the transaction response
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                // Payment profile code save here
                Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                try {
                    if (customers != null) {
                        customers.setAuthorizeCustomerProfileId(response.getCustomerProfileId());
                        customers.setAuthorizeCustomerPaymentProfileId(response.getCustomerPaymentProfileIdList().getNumericString().get(0));
                        this.customersRepository.save(customers);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error while saving customer payment profile ID: " + e.getMessage());
                    return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Save Customer Payment Profile", e.getMessage());
                }

                return new ApiResponse<>(HttpStatus.OK.value(), "Add Credit Card Profile Successfully.", response);
            } else {
                String errMsg = "";
                if (response.getMessages().getMessage() != null && !response.getMessages().getMessage().isEmpty()) {
                    String errCode = response.getMessages().getMessage().get(0).getCode();
                    String errText = response.getMessages().getMessage().get(0).getText();

                    if ("E00039".equals(errCode)) {
                        // Extract customerProfileId from errText using regex matcher
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(errText);
                        if (matcher.find()) {
                            String existingCustomerProfileId = matcher.group();

                            // Try to create the payment profile for the existing customer profile
                            CreateCustomerPaymentProfileRequest paymentProfileRequest = new CreateCustomerPaymentProfileRequest();
                            paymentProfileRequest.setMerchantAuthentication(merchantAuthenticationType);
                            paymentProfileRequest.setCustomerProfileId(existingCustomerProfileId);
                            paymentProfileRequest.setPaymentProfile(customerPaymentProfileType);
                            paymentProfileRequest.setValidationMode(paymentGatewayConfiguration.validationModeEnumCheck());

                            CreateCustomerPaymentProfileController paymentProfileController = new CreateCustomerPaymentProfileController(paymentProfileRequest);
                            paymentProfileController.execute();

                            CreateCustomerPaymentProfileResponse paymentProfileResponse = paymentProfileController.getApiResponse();

                            if (paymentProfileResponse != null) {
                                if (paymentProfileResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                                    // Save the profile info to the database
                                    Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                                    try {
                                        if (customers != null) {
                                            customers.setAuthorizeCustomerProfileId(existingCustomerProfileId);
                                            customers.setAuthorizeCustomerPaymentProfileId(paymentProfileResponse.getCustomerPaymentProfileId());
                                            this.customersRepository.save(customers);
                                        }
                                    } catch (Exception e) {
                                        log.error("Error while saving customer payment profile ID: " + e.getMessage());
                                        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Save Customer Payment Profile", e.getMessage());
                                    }
                                    return new ApiResponse<>(HttpStatus.OK.value(), "Add Credit Card Profile Successfully.", paymentProfileResponse);
                                } else {
                                    // If creating the payment profile failed because it already exists
                                    String payProfileErrCode = paymentProfileResponse.getMessages().getMessage().get(0).getCode();
                                    if ("E00039".equals(payProfileErrCode)) {
                                        // Retrieve existing payment profile ID
                                        GetCustomerProfileRequest getProfileRequest = new GetCustomerProfileRequest();
                                        getProfileRequest.setMerchantAuthentication(merchantAuthenticationType);
                                        getProfileRequest.setCustomerProfileId(existingCustomerProfileId);
                                        GetCustomerProfileController getProfileController = new GetCustomerProfileController(getProfileRequest);
                                        getProfileController.execute();
                                        GetCustomerProfileResponse getProfileResponse = getProfileController.getApiResponse();

                                        if (getProfileResponse != null && getProfileResponse.getMessages().getResultCode() == MessageTypeEnum.OK) {
                                            if (getProfileResponse.getProfile() != null && getProfileResponse.getProfile().getPaymentProfiles() != null && !getProfileResponse.getProfile().getPaymentProfiles().isEmpty()) {
                                                String existingPaymentProfileId = getProfileResponse.getProfile().getPaymentProfiles().get(0).getCustomerPaymentProfileId();

                                                Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                                                try {
                                                    if (customers != null) {
                                                        customers.setAuthorizeCustomerProfileId(existingCustomerProfileId);
                                                        customers.setAuthorizeCustomerPaymentProfileId(existingPaymentProfileId);
                                                        this.customersRepository.save(customers);
                                                    }
                                                } catch (Exception e) {
                                                    log.error("Error while saving customer payment profile ID: " + e.getMessage());
                                                    return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Save Customer Payment Profile", e.getMessage());
                                                }
                                                return new ApiResponse<>(HttpStatus.OK.value(), "Add Credit Card Profile Successfully.", getProfileResponse);
                                            }
                                        }
                                    }
                                    errMsg = paymentProfileResponse.getMessages().getMessage().get(0).getText();
                                    return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Create Payment Profile For Existing Customer Profile", errMsg);
                                }
                            } else {
                                ANetApiResponse errorResponse = paymentProfileController.getErrorResponse();
                                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Create Payment Profile For Existing Customer Profile", errorResponse);
                            }
                        }
                    }
                }

                // If it is not duplicate or if recovery failed, fall back to parsing validation errors safely
                if (response.getValidationDirectResponseList() != null && 
                    response.getValidationDirectResponseList().getString() != null && 
                    !response.getValidationDirectResponseList().getString().isEmpty()) {
                    
                    String[] errList = response.getValidationDirectResponseList().getString().toArray(new String[0]);
                    if (errList.length > 0 && errList[0] != null) {
                        String[] err = errList[0].split(",");
                        if (err.length > 2) {
                            if (err[2].equals("7")) {
                                errMsg = "Credit Card Expiration Date Is Invalid";
                            } else if (err[2].equals("6") || err[2].equals("37")) {
                                errMsg = "The Credit Card Number Is Invalid";
                            } else if (err[2].equals("165") || (response.getMessages().getMessage() != null && !response.getMessages().getMessage().isEmpty() && "This Transaction Has Been Declined.".equals(response.getMessages().getMessage().get(0).getText()))) {
                                errMsg = "Please Confirm Your CVV Is Correct";
                            }
                        }
                    }
                }
                
                if (errMsg.isEmpty() && response.getMessages().getMessage() != null && !response.getMessages().getMessage().isEmpty()) {
                    errMsg = response.getMessages().getMessage().get(0).getText();
                }
                
                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Create Customer Profile", errMsg);
            }
        } else {
            ANetApiResponse errorResponse = controller.getErrorResponse();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Get Response", errorResponse);
        }
    }

    @GetMapping("/getPaymentProfile")
    public ApiResponse<?> getPaymentProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> resBody = new HashMap<>();
        HashMap<String, String> list = new HashMap<String, String>();
        if (userId > 0) {
            String authCustomerProfileId = customers.getAuthorizeCustomerProfileId();
            if (authCustomerProfileId == null || !authCustomerProfileId.matches("\\d+")) {
                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid Customer Profile.", "Invalid Payment Profile");
            }

            ApiOperationBase.setEnvironment(paymentGatewayConfiguration.environmentModeCheck());

            MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
            merchantAuthenticationType.setName(paymentGatewayConfiguration.authorizenetLonginId());
            merchantAuthenticationType.setTransactionKey(paymentGatewayConfiguration.authorizenetTransactionKey());
            ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

            GetCustomerProfileRequest apiRequest = new GetCustomerProfileRequest();
            apiRequest.setCustomerProfileId(authCustomerProfileId); //customerProfileId);

            GetCustomerProfileController controller = new GetCustomerProfileController(apiRequest);
            controller.execute();

            GetCustomerProfileResponse response = new GetCustomerProfileResponse();
            response = controller.getApiResponse();

            if (response != null) {

                if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                    if ((!response.getProfile().getPaymentProfiles().isEmpty()) &&
                            (response.getProfile().getPaymentProfiles().get(0).getBillTo() != null)) {
                        list.put("firstName", response.getProfile().getPaymentProfiles().get(0).getBillTo().getFirstName());
//                        list.put("lastName", response.getProfile().getPaymentProfiles().get(0).getBillTo().getLastName());
                        list.put("businessName", response.getProfile().getPaymentProfiles().get(0).getBillTo().getCompany());
                        list.put("address", response.getProfile().getPaymentProfiles().get(0).getBillTo().getAddress());
                        list.put("city", response.getProfile().getPaymentProfiles().get(0).getBillTo().getCity());
                        list.put("state", response.getProfile().getPaymentProfiles().get(0).getBillTo().getState());
                        list.put("postCode", response.getProfile().getPaymentProfiles().get(0).getBillTo().getZip());
                        list.put("country", response.getProfile().getPaymentProfiles().get(0).getBillTo().getCountry());
                        list.put("email", response.getProfile().getPaymentProfiles().get(0).getBillTo().getEmail());
                        String phone = response.getProfile().getPaymentProfiles().get(0).getBillTo().getPhoneNumber();
                        if (phone != null) {
                            phone = phone.replaceAll("-", "");
                        }
                        list.put("phone", phone);
                        list.put("cardNumber", response.getProfile().getPaymentProfiles().get(0).getPayment().getCreditCard().getCardNumber());
                        list.put("expMonthYear", response.getProfile().getPaymentProfiles().get(0).getPayment().getCreditCard().getExpirationDate());
                        list.put("cardType", response.getProfile().getPaymentProfiles().get(0).getPayment().getCreditCard().getCardType());
                        resBody.put("paymentProfile", list);
                    }
                } else {
                    return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid Customer Profile.", response.getMessages().getResultCode());
                }
                return new ApiResponse<>(HttpStatus.OK.value(), "Fetch Payment Profile Successfully.", resBody);
            }
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid Customer Profile.", "Invalid Payment Profile");
        } else {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Invalid Customer Profile.", "Invalid Payment Profile");
        }
    }

    @DeleteMapping("/deletePaymentProfile")
    public ApiResponse<?> deletePaymentProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (userId > 0) {
            String authCustomerProfileId = customers.getAuthorizeCustomerProfileId();
            if (authCustomerProfileId == null || !authCustomerProfileId.matches("\\d+")) {
                try {
                    customers.setAuthorizeCustomerProfileId("");
                    customers.setAuthorizeCustomerPaymentProfileId("");
                    customersRepository.save(customers);
                } catch (Exception e) {
                    log.error("[ customerId : " + customers + " ] DeletePaymentProfile Error : " + e);
                }
                return new ApiResponse<>(HttpStatus.OK.value(), "Delete Payment Profile Successfully.", null);
            }

            ApiOperationBase.setEnvironment(paymentGatewayConfiguration.environmentModeCheck());

            MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
            merchantAuthenticationType.setName(paymentGatewayConfiguration.authorizenetLonginId());
            merchantAuthenticationType.setTransactionKey(paymentGatewayConfiguration.authorizenetTransactionKey());
            ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

            DeleteCustomerProfileRequest apiRequest = new DeleteCustomerProfileRequest();
            apiRequest.setCustomerProfileId(authCustomerProfileId);

            DeleteCustomerProfileController controller = new DeleteCustomerProfileController(apiRequest);
            controller.execute();

            DeleteCustomerProfileResponse response = new DeleteCustomerProfileResponse();
            response = controller.getApiResponse();

            if (response != null) {
                if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                    try {
                        if (customers != null) {
                            customers.setAuthorizeCustomerProfileId("");
                            customers.setAuthorizeCustomerPaymentProfileId("");
                            customersRepository.save(customers);
                        }
                    } catch (Exception e) {
                        log.error("[ customerId : " + customers + " ] DeletePaymentProfile Error : " + e);
                    }
                    return new ApiResponse<>(HttpStatus.OK.value(), "Delete Payment Profile Successfully.", response);
                } else {
                    log.error("[ customerId : " + customers + " ] Failed To Delete Customer Profile  Error : " + response.getMessages().getResultCode());
                    return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Delete Customer Profile.", response.getMessages().getResultCode());
                }
            }
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Delete Customer Profile.", "Failed To Delete Customer Profile.");
        } else {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Delete Customer Profile.", "Failed To Delete Customer Profile.");
        }
    }

    @PostMapping("/updatePaymentProfile")
    public ApiResponse<?> updatePaymentProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody @Valid PaymentGatewayRequestDto paymentGatewayRequestDto) {
        String[] cardExp = paymentGatewayRequestDto.getExpMonthYear().split("/");

        int expYear = Integer.parseInt(cardExp[1]);
        int expMonth = Integer.parseInt(cardExp[0]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR) % 100;
        int month = calendar.get(Calendar.MONTH);

        if (year >= expYear) {
            if (expMonth <= (month + 1)) {
                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Please Confirm The Expiration Date Of Your Credit Card Is Correct And Matches The Format Of MM/YYYY", "Error");
            }
        }
        Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        Customers memberDto = this.customersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String authCustomerProfileId = memberDto.getAuthorizeCustomerProfileId();
        String authCustomerPaymentProfileId = memberDto.getAuthorizeCustomerPaymentProfileId();

        if (authCustomerProfileId == null || !authCustomerProfileId.matches("\\d+")) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Update Customer Profile", "No valid customer profile exists.");
        }

        // Set the request to operate in either the sandbox or production environment
        ApiOperationBase.setEnvironment(paymentGatewayConfiguration.environmentModeCheck());

        // Create object with merchant authentication details
        MerchantAuthenticationType merchantAuthenticationType = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(paymentGatewayConfiguration.authorizenetLonginId());
        merchantAuthenticationType.setTransactionKey(paymentGatewayConfiguration.authorizenetTransactionKey());
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        // Set Me
        CustomerAddressType customerAddressType = new CustomerAddressType();
        customerAddressType.setFirstName(paymentGatewayRequestDto.getName());
//        customerAddressType.setLastName(paymentGatewayRequestDto.getLastName());
        customerAddressType.setCompany(paymentGatewayRequestDto.getBusinessName());
        customerAddressType.setAddress(paymentGatewayRequestDto.getAddress());
        customerAddressType.setCity(paymentGatewayRequestDto.getCity());
        customerAddressType.setState(paymentGatewayRequestDto.getState());
        customerAddressType.setCountry(paymentGatewayRequestDto.getCountry());
        customerAddressType.setPhoneNumber(paymentGatewayRequestDto.getPhone());
        customerAddressType.setZip(paymentGatewayRequestDto.getPostCode());

        //credit card details
        CreditCardType creditCard = new CreditCardType();
        creditCard.setCardNumber(paymentGatewayRequestDto.getCardNumber());
        creditCard.setExpirationDate(cardExp[1] + "-" + cardExp[0]);
        creditCard.setCardCode(paymentGatewayRequestDto.getCardCode());

        PaymentType paymentType = new PaymentType();
        paymentType.setCreditCard(creditCard);

        CustomerPaymentProfileExType customer = new CustomerPaymentProfileExType();
        customer.setPayment(paymentType);
        customer.setCustomerPaymentProfileId(authCustomerPaymentProfileId);
        customer.setBillTo(customerAddressType);

        UpdateCustomerPaymentProfileRequest apiRequest = new UpdateCustomerPaymentProfileRequest();
        apiRequest.setCustomerProfileId(authCustomerProfileId);
        apiRequest.setPaymentProfile(customer);
        apiRequest.setValidationMode(paymentGatewayConfiguration.validationModeEnumCheck());

        UpdateCustomerPaymentProfileController controller = new UpdateCustomerPaymentProfileController(apiRequest);
        controller.execute();

        UpdateCustomerPaymentProfileResponse response = new UpdateCustomerPaymentProfileResponse();
        response = controller.getApiResponse();

        if (response != null) {
            if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                // System.out.println(response.getMessages().getMessage().get(0).getCode());
                // System.out.println(response.getMessages().getMessage().get(0).getText());
                return new ApiResponse<>(HttpStatus.OK.value(), "Update Credit Card Profile Successfully.", response);
            } else {
                String[] err = response.getValidationDirectResponse().split(",");
                String errMsg = "";
                if (err[2].equals("7")) {
                    errMsg = "Credit Card Expiration Date Is Invalid";
                } else if (err[2].equals("6") || err[2].equals("37")) {
                    errMsg = "The Credit Card Number Is Invalid";
                } else if (err[2].equals("165") || response.getMessages().getMessage().get(0).getText().equals("This Transaction Has Been Declined.")) {
                    errMsg = "Please Confirm Your CVV Is Correct";
                } else {
                    errMsg = response.getMessages().getMessage().get(0).getText();
                }
                return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Update Customer Profile", errMsg);
            }
        }
        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed To Update Customer Payment Profile.", "Failed To Update Customer Payment Profile.");
    }
}
