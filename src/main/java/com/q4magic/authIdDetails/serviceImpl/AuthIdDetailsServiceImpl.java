package com.q4magic.authIdDetails.serviceImpl;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.authId.AuthId;
import com.q4magic.authIdDetails.service.AuthIdDetailsService;
import com.q4magic.common.dto.AuthIdDetailsDto;
import com.q4magic.common.dto.CustomersDto;
import com.q4magic.common.models.AuthIDetails;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.AuthIDetailsRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.subUserType.service.SubUserTypeService;
import kong.unirest.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service(value = "authIdDetailsService")
public class AuthIdDetailsServiceImpl implements AuthIdDetailsService {
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");

    @Value("${zeroBounceBaseUrl}")
    private String zeroBounceBaseUrl;

    @Value("${zeroBounceApiKey}")
    private String zeroBounceApiKey;

    @Value("${proofyBaseUrl}")
    private String proofyBaseUrl;

    @Value("${proofyApiKey}")
    private String proofyApiKey;

    @Value("${proofyAId}")
    private String proofyAId;

    @Value("${authIdAccountNumberStart}")
    private String authIdAccountNumberStart;

    @Value("${authIdExternalId}")
    private String authIdExternalId;

    @Value("${authIdApiKeyValue}")
    private String authIdApiKeyValue;

    @Autowired
    private AuthIDetailsRepository authIdDetailsRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private SubUserTypeService subUserTypeService;

    @Override
    public Map<String, Object> getAuthDetails(String email) {
        Map<String, Object> resBody = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        resBody.put("userInfo", jsonArray);
        resBody.put("error", "");
        try {
            AuthIDetails authIdDetails = this.authIdDetailsRepository.findByEmail(email);
            if (authIdDetails != null) {
                resBody = AuthId.authIdGetProofResultsAllData(authIdExternalId, authIdApiKeyValue, authIdDetails.getAuthOperationId());
                if (resBody.get("error").equals("Conflict")) {
                    resBody.put("userInfo", jsonArray);
                    resBody.put("error", "");
                }
            }
            return resBody;
        } catch (Exception e) {
            errorLogger.error("getAuthDetails service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> createAuthDetails(AuthIdDetailsDto authIdDetailsDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("userData", "");
        resBody.put("operationId", "");
        resBody.put("oneTimeSecret", "");
        String accNo = authIdAccountNumberStart + "-" + authIdDetailsDto.getEmail();
        try {
            try {
                AuthId.authIdDeleteAccount(authIdExternalId, authIdApiKeyValue, accNo);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            AuthIDetails authIdDetails = new AuthIDetails();
            BeanUtils.copyProperties(authIdDetailsDto, authIdDetails);
            authIdDetails.setRegisteredDate(new Timestamp(System.currentTimeMillis()));
            resBody = AuthId.authIdCreateAccount(authIdExternalId, authIdApiKeyValue, authIdDetailsDto, accNo);
            authIdDetails.setAuthAccountNumber(resBody.get("accountNumber").toString());
            authIdDetails = this.authIdDetailsRepository.save(authIdDetails);
            BeanUtils.copyProperties(authIdDetails, authIdDetailsDto);
            if (authIdDetailsDto.getAuthAccountNumber() != null && !authIdDetailsDto.getAuthAccountNumber().isEmpty()) {
                resBody = AuthId.authIdGetForeignIDDocument(authIdExternalId, authIdApiKeyValue, authIdDetailsDto.getAuthAccountNumber(), String.valueOf(authIdDetailsDto.getDocumentType()));
            }
            resBody.put("userData", authIdDetailsDto);
            return resBody;
        } catch (Exception e) {
            errorLogger.error("createAuthDetails service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> updateAuthDetails(AuthIdDetailsDto authIdDetailsDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("userData", "");
        resBody.put("error", "");
        try {
            resBody = AuthId.authIdGetProofResults(authIdExternalId, authIdApiKeyValue, authIdDetailsDto.getAuthOperationId());
            AuthIDetails authIdDetails = this.authIdDetailsRepository.findById(authIdDetailsDto.getId()).orElseThrow(() -> new RuntimeException("AuthIdDetails not found"));

            if (Boolean.parseBoolean(resBody.get("verified").toString())) {
                if (authIdDetailsDto.getAuthOperationId().isEmpty()) {
                    authIdDetails.setAuthSelfieOperationId(authIdDetailsDto.getAuthSelfieOperationId());
                    throw new RuntimeException("OperationId is required");
                } else {
                    authIdDetails.setAuthOperationId(authIdDetailsDto.getAuthOperationId());
                }
                this.authIdDetailsRepository.save(authIdDetails);
                AuthIdDetailsDto authIdDetailsDto1 = new AuthIdDetailsDto();
                BeanUtils.copyProperties(authIdDetails, authIdDetailsDto1);
                Map<String, Object> data = AuthId.authIdGetProofResultsAllData(authIdExternalId, authIdApiKeyValue, authIdDetails.getAuthOperationId());
                Map<String, Object> data2 = AuthId.authIdGetProofTempId(authIdExternalId, authIdApiKeyValue, authIdDetails.getAuthAccountNumber(), authIdDetails.getAuthOperationId());
                resBody.put("userData", authIdDetailsDto1);
                resBody.put("authUserData", data);
                resBody.put("authTempData", data2);
            } else {
                resBody.put("error", "error1");
            }
            return resBody;

        } catch (Exception e) {
            errorLogger.error("updateAuthDetails service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteAuthDetails(Integer authId) {
        try {
            AuthIDetails authIdDetails = this.authIdDetailsRepository.findById(authId).orElseThrow(() -> new RuntimeException("AuthIdDetails not found"));
            this.authIdDetailsRepository.delete(authIdDetails);
        } catch (Exception e) {
            errorLogger.error("deleteAuthDetails service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> login(String email) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("userData", "");
        resBody.put("error", "");
        resBody.put("customerData", "");
        try {
            Customers customers = this.customersRepository.findByEmail(email, null);
            if (customers == null) {
                customers = this.customersRepository.findByUserName(email, null);
            }
            int flag = 0;
            AuthIdDetailsDto authIdDetailsDto = new AuthIdDetailsDto();
            AuthIDetails findUserData = null;
            try {
                findUserData = this.authIdDetailsRepository.findByEmail(customers.getEmailAddress());
            } catch (Exception e) {
                errorLogger.error("login service Error: " + e);
            }

            if (findUserData != null) {
                if (findUserData.getAuthAccountNumber() != null && !findUserData.getAuthAccountNumber().isEmpty()) {
                    resBody = AuthId.authIdVerifyIdentity(authIdExternalId, authIdApiKeyValue, findUserData.getAuthAccountNumber());
                    if (resBody.get("error").equals("")) {
                        BeanUtils.copyProperties(findUserData, authIdDetailsDto);
                    } else {
                        if (findUserData.getEmail().equalsIgnoreCase("ritesh@ematrixinfotech.com")) {
                            resBody.put("error", "Email address already exists please use another email");
                            BeanUtils.copyProperties(findUserData, authIdDetailsDto);
                            resBody.put("userData", authIdDetailsDto);
                            return resBody;
                        } else {
                            flag = 1;
                            if (findUserData.getAuthAccountNumber() != null && !findUserData.getAuthAccountNumber().trim().isEmpty()) {
                                try {
                                    AuthId.authIdDeleteAccount(authIdExternalId, authIdApiKeyValue, findUserData.getAuthAccountNumber().trim());
                                } catch (Exception ee) {
                                    throw new RuntimeException(ee.getMessage());
                                }
                            }
                            try {
                                this.authIdDetailsRepository.deleteById(findUserData.getId());
                            } catch (Exception ee) {
                                throw new RuntimeException(ee.getMessage());
                            }
                        }
                    }
                } else {
                    flag = 1;
                }
            } else {
                flag = 1;
            }

            if (flag == 1) {
                // Add Default AuthId Account Number
                String authidAccountNumber = "dev-nosql-ritesh@ematrixinfotech.com";
                resBody = AuthId.authIdVerifyIdentity(authIdExternalId, authIdApiKeyValue, authidAccountNumber);
            }
            resBody.put("userData", authIdDetailsDto);
            if (customers != null) {

                CustomersDto customer1 = new CustomersDto();
                BeanUtils.copyProperties(customers, customer1);
                customer1.setPassword(null);
                final String jwtToken = jwtUtil.generateToken(customers);
                resBody.put("authToken", jwtToken);
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("userId", customers.getId());
                customerData.put("username", customers.getUsername());
                customerData.put("email", customers.getEmailAddress());
                if (customers.getRole() != null) {
                    customerData.put("subUser", false);
                    customerData.put("roleId", customers.getRole().getId());
                    customerData.put("roleName", customers.getRole().getRole());
                } else {
                    customerData.put("subUser", true);
                    customerData.put("roleId", customers.getSubUserType().getId());
                    customerData.put("roleName", customers.getSubUserType().getName());
                    customerData.put("permissions", this.subUserTypeService.getSubUserTypeById(customers.getSubUserType().getId()));
                }
                resBody.put("customerData", customerData);
            }

        } catch (Exception e) {
            resBody.put("error", "Invalid Data");
        }
        return resBody;
    }
}