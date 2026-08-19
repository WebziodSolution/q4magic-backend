package com.q4magic.customers.serviceImpl;

import com.q4magic.account.service.AccountService;
import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.authId.ZeroBounce.ZeroBounce;
import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.customerQuota.service.CustomerQuotaService;
import com.q4magic.customers.service.CustomersService;
import com.q4magic.subUserType.service.SubUserTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service(value = "customersService")
public class CustomersServiceImpl implements CustomersService {
    private static final String SECRET_KEY = "your-very-secret-key";

    @Value("${zeroBounceBaseUrl}")
    String zeroBounceBaseUrl;

    @Value("${zeroBounceApiKey}")
    String zeroBounceApiKey;

    @Value("${siteUrl}")
    String siteUrl;

    @Value("${token.reset.expiration}")
    private long resetTokenExpiration;

    @Value("${token.subuser.expiration}")
    private long subUserTokenExpiration;

    private final Random random = new Random();

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private RoleLookupRepository roleLookupRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private AuthIDetailsRepository authIdDetailsRepository;

    @Autowired
    private BusinessInfoRepository businessInfoRepository;

    @Autowired
    private SubUserTypeService subUserTypeService;

    @Autowired
    private SubUserTypeRepository subUserTypeRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerQuotaService customerQuotaService;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Autowired
    private CustomerQuotaRepository customerQuotaRepository;

    @Autowired
    private SubscriptionRatesRepository subscriptionRatesRepository;

    @Override
    public Map<String, Object> getUserSalesForceToken(Integer id) {
        try {
            Map<String, Object> map = new HashMap<>();
            Customers customers = this.customersRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            map.put("accessToken_salesforce", customers.getSalesforceAccessToken());
            map.put("instanceUrl_salesforce", customers.getSalesforceInstanceUrl());
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomerDashboardDto getDashboardData(Integer customerId, CustomerDashboardRequestDto req) {
        try {
            Customers customers = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            if (customers.getRole() != null && customers.getRole().getRole().equals("SALES MANAGER")) {
                return getManagerDashboardData(customerId, req);
            } else {
                CustomerDashboardDto dto = new CustomerDashboardDto();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

                Timestamp startTs = null;
                Timestamp endTs = null;

                if (req != null
                        && req.getStartDate() != null
                        && req.getEndDate() != null
                        && !req.getStartDate().isBlank()
                        && !req.getEndDate().isBlank()) {

                    LocalDate start = LocalDate.parse(req.getStartDate().trim(), formatter);
                    LocalDate end = LocalDate.parse(req.getEndDate().trim(), formatter);

                    // inclusive range
                    startTs = Timestamp.valueOf(start.atStartOfDay());
                    endTs = Timestamp.valueOf(end.plusDays(1).atStartOfDay().minusNanos(1));
                }

                // -----------------------------------------
                // Fetch filtered data by created_at range
                // If no dates => fallback to existing logic
                // -----------------------------------------
                Integer contactCount = 0;
                Integer meetingCount = 0;
                Integer newMeetingCount = 0;
                Integer oldMeetingCount = 0;

                List<Opportunities> closedOpp;
                List<Opportunities> opportunities; // active
                List<Opportunities> allPipeLineOpportunities; // base list for pipeline total + pipeline rows
                List<Map<String, Object>> meetingData = new ArrayList<>();
                if (startTs != null && endTs != null) {
                    contactCount = this.contactsRepository.countContactsByCustomerAndDateRange(customerId, startTs,
                            endTs);
                    List<Meetings> meetings = this.meetingsRepository.countMeetingsByCustomerAndDateRange(customerId,
                            startTs, endTs);
                    if (!meetings.isEmpty()) {
                        meetingCount = meetings.size();
                        for (Meetings meeting : meetings) {
                            boolean hasNewContact = false;
                            if (meeting.getContactIds() != null && !meeting.getContactIds().isEmpty()) {
                                String contactIdsStr = meeting.getContactIds();
                                Integer[] contactIds = Arrays.stream(
                                        contactIdsStr.replace("[", "")
                                                .replace("]", "")
                                                .split(","))
                                        .map(String::trim)
                                        .map(Integer::valueOf)
                                        .toArray(Integer[]::new);

                                for (Integer contactId : contactIds) {
                                    Contacts contacts = this.contactsRepository
                                            .findById(contactId)
                                            .orElseThrow(() -> new RuntimeException("Contact not found"));

                                    Map<String, Object> meetingObj = new HashMap<>();
                                    meetingObj.put("id", contacts.getAccount().getId());
                                    meetingObj.put("account_name", contacts.getAccount().getAccountName());
                                    meetingData.add(meetingObj);

                                    if (Boolean.TRUE.equals(contacts.getFromMailScraping())) {
                                        hasNewContact = true;
                                        break; // ✅ one true is enough
                                    }
                                }
                            }

                            if (hasNewContact) {
                                newMeetingCount++;
                            } else {
                                oldMeetingCount++;
                            }
                        }
                    }
                    closedOpp = this.opportunitiesRepository.findClosedOpportunitiesByCustomerAndDateRange(customerId,
                            startTs, endTs);
                    opportunities = this.opportunitiesRepository.findByCustomerAndDateRange(customerId, startTs, endTs);
                    allPipeLineOpportunities = this.opportunitiesRepository
                            .findActiveOpportunitiesByCustomerAndDateRange(customerId, startTs, endTs);
                } else {
                    // Old behavior (no date filter)
                    contactCount = this.contactsRepository.countNewContactByCustomerId(customerId);
                    meetingCount = this.meetingsRepository.countMeetingByCustomerId(customerId);

                    closedOpp = this.opportunitiesRepository.closeDealOpportunitiesByCustomerId(customerId);
                    opportunities = this.opportunitiesRepository.findActiveOpportunities(customerId);

                    // NOTE: your original code uses findAll() which includes all customers.
                    // To keep logic consistent but correct, prefer customer scope:
                    allPipeLineOpportunities = this.opportunitiesRepository.findActiveOpportunities(customerId);
                }

                Integer totalDealAmount = 0;
                Integer totalClosedDealAmount = 0;
                Integer pipeLineOpportunitiesTotal = 0;
                List<Map<String, Object>> pipeLineData = new ArrayList<>();

                // Closed deal amount
                for (Opportunities opp : closedOpp) {
                    if (opp.getDealAmount() != null) {
                        totalClosedDealAmount += Integer.parseInt(opp.getDealAmount().toString());
                    }
                }

                // Pipeline deal amount (from active list)
                for (Opportunities opp : opportunities) {
                    if (opp.getDealAmount() != null && "Pipeline".equals(opp.getStatus())) {
                        totalDealAmount += Integer.parseInt(opp.getDealAmount().toString());
                    }
                }
                // Total pipeline amount base (same as your original pattern)
                for (Opportunities opp : allPipeLineOpportunities) {
                    if (opp.getDealAmount() != null) {
                        pipeLineOpportunitiesTotal += Integer.parseInt(opp.getDealAmount().toString());
                    }
                }

                // Pipeline rows
                for (Opportunities opp : allPipeLineOpportunities) {
                    if ("Pipeline".equals(opp.getStatus())) {
                        Map<String, Object> pipeLine = new HashMap<>();
                        pipeLine.put("id", opp.getId());
                        pipeLine.put("name", opp.getOpportunity());
                        pipeLine.put("dealAmount", opp.getDealAmount());
                        pipeLine.put("created_by",
                                opp.getCustomers() != null ? opp.getCustomers().getUsername() : null);

                        if (opp.getAccount() != null) {
                            pipeLine.put("account", opp.getAccount().getAccountName());
                        }
                        pipeLineData.add(pipeLine);
                    }
                }

                dto.setCustomerId(customerId);
                dto.setTotalContacts(contactCount);
                dto.setTotalPipeLine(pipeLineOpportunitiesTotal);
                dto.setTotalMeetings(meetingCount);
                dto.setTotalNewMeetings(newMeetingCount);
                dto.setTotalOldMeetings(oldMeetingCount);
                dto.setTotalClosedDealAmount(totalClosedDealAmount);
                dto.setTotalDealAmount(totalDealAmount);
                dto.setPipeLineData(pipeLineData);
                dto.setMeetingData(meetingData);
                return dto;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CustomersDto> getAllSubUsers(Integer id) {
        try {
            Customers customers = this.customersRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            List<Customers> customersList = this.customersRepository.getAllSubUsers(id);
            // if (customersList.isEmpty()) {
            // if (customers.getCustomers() != null) {
            // customersList =
            // this.customersRepository.getAllSubUsers(customers.getCustomers().getId());
            // }
            // }
            List<CustomersDto> customersDtoList = new java.util.ArrayList<>();
            if (!customersList.isEmpty()) {
                for (Customers customer : customersList) {
                    customersDtoList.add(this.getCustomerById(customer.getId()));
                }
            }
            return customersDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CustomersDto> getAllSubUsersWithParntSubUser(Integer userId) {
        try {
            Customers me = customersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Map to keep sub-users unique by their id, while preserving order
            Map<Integer, Customers> uniqueUsers = new LinkedHashMap<>();

            // 1) Add logged-in user's own sub-users
            List<Customers> mySubUsers = customersRepository.getAllSubUsers(userId);
            if (mySubUsers != null) {
                for (Customers c : mySubUsers) {
                    uniqueUsers.put(c.getId(), c);
                }
            }

            // 2) If the logged-in user has a parent customer, fetch the parent's sub-users
            // as well
            if (me.getCustomers() != null) {
                Integer parentId = me.getCustomers().getId();
                List<Customers> parentSubUsers = customersRepository.getAllSubUsers(parentId);
                if (parentSubUsers != null) {
                    for (Customers c : parentSubUsers) {
                        uniqueUsers.put(c.getId(), c);
                    }
                }
            }

            List<CustomersDto> dtoList = new ArrayList<>();
            Set<Integer> addedIds = new HashSet<>();

            dtoList.add(this.getCustomerById(userId));
            addedIds.add(userId);

            for (Integer id : uniqueUsers.keySet()) {
                if (!addedIds.contains(id)) {
                    dtoList.add(this.getCustomerById(id));
                    addedIds.add(id);
                }
            }
            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CustomersDto> getAllCustomers() {
        try {
            List<Customers> customersList = this.customersRepository.findAll();
            List<CustomersDto> customersDtoList = new java.util.ArrayList<>();
            if (!customersList.isEmpty()) {
                for (Customers customer : customersList) {
                    customersDtoList.add(this.getCustomerById(customer.getId()));
                }
            }
            return customersDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomersDto getCustomerByEmail(String email) {
        try {
            Customers user = this.customersRepository.findByEmail(email, null);
            CustomersDto customersDto = new CustomersDto();
            if (user != null) {
                customersDto.setId(user.getId());
                customersDto.setEmailAddress(user.getEmailAddress());
            }
            return customersDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomersDto getCustomerById(Integer id) {
        try {
            CustomersDto customersDto = new CustomersDto();
            Customers customer = this.customersRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            BusinessInfo businessInfo = this.businessInfoRepository.findByCustomerId(id);

            if (businessInfo != null) {
                BusinessInfoDto businessInfoDto = new BusinessInfoDto();
                businessInfoDto.setCusId(id);
                BeanUtils.copyProperties(businessInfo, businessInfoDto);
                customersDto.setBusinessInfo(businessInfoDto);
            }
            if (customer.getRole() != null) {
                customersDto.setRoleId(customer.getRole().getId());
            }
            if (customer.getEvalPeriod() != null) {
                customersDto.setEvalPeriod(this.commonService.convertDateToString(customer.getEvalPeriod()));
            }

            if (customer.getDateRegistered() != null) {
                customersDto.setDateRegistered(this.commonService.convertDateToString(customer.getDateRegistered()));
            }

            if (customer.getSubscriptionRates() != null) {
                customersDto.setPlanId(customer.getSubscriptionRates().getId());
            }
            if (customer.getReportTo() != null) {
                customersDto.setReportTo(customer.getReportTo().getId());
            }
            if (customer.getCustomers() != null) {
                customersDto.setParentUserId(customer.getCustomers().getId());
                // Map<String, Object> res =
                // this.accountService.getAccountByName(customer.getCustomers().getId());
                // if (res.containsKey("success") && (Boolean) res.get("success")) {
                // AccountDto accountDto = this.accountService.getAccountById((Integer)
                // res.get("id"));
                // customersDto.setCrmId(accountDto.getCrmId());
                // }
            }
            if (customer.getSubUserType() != null) {
                customersDto.setSubUserTypeId(customer.getSubUserType().getId());
                customersDto.setSubUserTypeDto(
                        this.subUserTypeService.getSubUserTypeById(customer.getSubUserType().getId()));
            }

            if (customer.getStartEvalPeriod() != null) {
                customersDto.setStartEvalPeriod(this.commonService.convertDateToString(customer.getStartEvalPeriod()));
            }
            if (customer.getEndEvalPeriod() != null) {
                customersDto.setEndEvalPeriod(this.commonService.convertDateToString(customer.getEndEvalPeriod()));
            }
            List<CustomerQuotaDto> customerQuotaDto = this.customerQuotaService.getAllCustomerQuotas(id);
            if (!customerQuotaDto.isEmpty()) {
                customersDto.setCustomerQuotaDto(customerQuotaDto);
            }
            BeanUtils.copyProperties(customer, customersDto);
            return customersDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomersDto createCustomer(CustomersDto customersDto, String type, Boolean createAccount) {
        try {
            Customers customer = new Customers();
            if (customersDto.getRoleId() != null) {
                RoleLookup roleLookup = roleLookupRepository.findById(customersDto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                customer.setRole(roleLookup);
            }

            if (customersDto.getEvalPeriod() != null && !customersDto.getEvalPeriod().isBlank()) {
                customer.setEvalPeriod(this.commonService.convertStringToDate(customersDto.getEvalPeriod()));
            }

            if (customersDto.getCalendarYearType() != null && customersDto.getCalendarYearType() != "") {
                customer.setQuota("Y");
                customer.setStartEvalPeriod(this.commonService.convertStringToDate(customersDto.getStartEvalPeriod()));
                customer.setEndEvalPeriod(this.commonService.convertStringToDate(customersDto.getEndEvalPeriod()));
            } else {
                customer.setQuota("N");
                customer.setStartEvalPeriod(null);
                customer.setEndEvalPeriod(null);
            }

            customer.setDateRegistered(new Timestamp(System.currentTimeMillis()));
            LocalDateTime firstDayOfMonth = LocalDate.now()
                    .withDayOfMonth(1)
                    .atStartOfDay();

            customer.setBillDate(Timestamp.valueOf(firstDayOfMonth));

            if (type.equals("Subuser")) {
                customer.setAccountOwner("N");
                Customers parentCustomer = this.customersRepository.findById(customersDto.getParentUserId())
                        .orElseThrow(() -> new RuntimeException("Parent Customer not found"));
                customer.setCustomers(parentCustomer);
                SubUserType subUserType = this.subUserTypeRepository.findById(customersDto.getSubUserTypeId())
                        .orElseThrow(() -> new RuntimeException("SubUserType not found"));
                customer.setSubUserType(subUserType);
            } else {
                customer.setAccountOwner("Y");
                SubscriptionRates subscriptionRates = this.subscriptionRatesRepository
                        .findById(customersDto.getPlanId())
                        .orElseThrow(() -> new RuntimeException("Subscription Rates not found"));
                customer.setSubscriptionRates(subscriptionRates);
            }
            if (customersDto.getReportTo() != null) {
                Customers reportto = this.customersRepository.findById(customersDto.getReportTo())
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                customer.setReportTo(reportto);
            } else {
                customer.setReportTo(null);
            }
            // if (customersDto.getAuthId() != null) {
            // AuthIDetails authIdDetails =
            // this.authIdDetailsRepository.findById(customersDto.getAuthId()).orElseThrow(()
            // -> new RuntimeException("AuthIdDetails not found"));
            // customer.setAuthIDetails(authIdDetails);
            // }
            BeanUtils.copyProperties(customersDto, customer, "id", "accountOwner", "dateRegistered", "evalPeriod",
                    "role", "subscriptionRates", "reportTo");
            this.customersRepository.save(customer);
            if (customersDto.getParentUserId() == null) {
                sendWelcomeEmail(customer.getEmailAddress(), customer.getUsername());
            }
            customersDto.setId(customer.getId());
            return customersDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomersDto updateCustomer(Integer id, CustomersDto customersDto, String type) {
        try {
            Customers customer = this.customersRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            if (customersDto.getPlanId() != null) {
                SubscriptionRates subscriptionRates = this.subscriptionRatesRepository
                        .findById(customersDto.getPlanId())
                        .orElseThrow(() -> new RuntimeException("Subscription Rates not found"));
                customer.setSubscriptionRates(subscriptionRates);
            }
            if (customersDto.getRoleId() != null) {
                RoleLookup roleLookup = roleLookupRepository.findById(customersDto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                customer.setRole(roleLookup);
            }
            if (customersDto.getEvalPeriod() != null && !customersDto.getEvalPeriod().isBlank()) {
                customer.setEvalPeriod(this.commonService.convertStringToDate(customersDto.getEvalPeriod()));
            }
            if (customersDto.getCalendarYearType() != null) {
                customer.setQuota("Y");
                customer.setStartEvalPeriod(this.commonService.convertStringToDate(customersDto.getStartEvalPeriod()));
                customer.setEndEvalPeriod(this.commonService.convertStringToDate(customersDto.getEndEvalPeriod()));
            } else {
                customer.setQuota("N");
                customer.setStartEvalPeriod(null);
                customer.setEndEvalPeriod(null);
            }

            if (type.equals("Subuser")) {
                customer.setAccountOwner("N");
                if (customersDto.getSubUserTypeId() != null) {
                    SubUserType subUserType = this.subUserTypeRepository.findById(customersDto.getSubUserTypeId())
                            .orElseThrow(() -> new RuntimeException("SubUserType not found"));
                    customer.setSubUserType(subUserType);
                }
                customer.setFirstName(customer.getFirstName());
                customer.setLastName(customer.getLastName());
                customer.setTitle(customer.getTitle());

            } else {
                customer.setAccountOwner("Y");
            }
            if (customersDto.getBillingAddressSameAsPrimary() != null
                    && customersDto.getBillingAddressSameAsPrimary()) {
                customer.setBillingAddress1(customersDto.getAddress1());
                customer.setBillingAddress2(customersDto.getAddress2());
                customer.setBillingCity(customersDto.getBillingCity());
                customer.setBillingState(customersDto.getBillingState());
                customer.setBillingZipcode(customersDto.getBillingZipcode());
                customer.setBillingCountry(customersDto.getBillingCountry());
            } else {
                customer.setBillingAddress1(customersDto.getBillingAddress1());
                customer.setBillingAddress2(customersDto.getBillingAddress2());
                customer.setBillingCity(customersDto.getBillingCity());
                customer.setBillingState(customersDto.getBillingState());
                customer.setBillingZipcode(customersDto.getBillingZipcode());
                customer.setBillingCountry(customersDto.getBillingCountry());
            }
            if (customersDto.getReportTo() != null) {
                Customers reportto = this.customersRepository.findById(customersDto.getReportTo())
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                customer.setReportTo(reportto);
            } else {
                customer.setReportTo(null);
            }
            BeanUtils.copyProperties(customersDto, customer, "id", "accountOwner", "role", "customers", "evalPeriod",
                    "dateRegistered", "billingAddress1", "billingAddress2", "billingCity", "billingState",
                    "billingZipcode", "billingCountry", "subscriptionRates", "reportTo");
            this.customersRepository.save(customer);
            return customersDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCustomer(Integer id) {
        try {
            Customers customer = this.customersRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            this.customersRepository.delete(customer);
            // if (customer.getCustomers() != null) {
            // Map<String, Object> response =
            // this.accountService.getAccountByName(customer.getCustomers().getId());
            // if (response.containsKey("success") && (Boolean) response.get("success")) {
            // this.accountService.deleteAccount((Integer) response.get("id"), true);
            // }
            // }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Customers user = this.customersRepository.findByEmail(email, null);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    @Override
    public Map<String, Object> isEmailExits(String email, Integer userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Customers customer = this.customersRepository.findByEmail(email, userId);
            if (customer != null) {
                res.put("isEmailExits", email + " is already registered.");
                return res;
            }
            Map<String, Object> isValid = ZeroBounce.validate(zeroBounceBaseUrl, zeroBounceApiKey, email);
            if (isValid.get("status").equals(false)) {
                res.put("ZeroBounce",
                        "Sorry, this email is not supported. Additional verification will be required. Please try a different email address.");
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> isUserNameExits(String userName, Integer userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Customers customer = this.customersRepository.findByUserName(userName, userId);
            if (customer != null) {
                res.put("isUserNameExits", "Username " + userName + " is already registered.");
                return res;
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> userLogin(LoginDto loginDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            if ((loginDto.getPassword() == null || loginDto.getPassword().isBlank()) && loginDto.getEmail() != null) {
                Customers customerByEmail = this.customersRepository.findByEmail(loginDto.getEmail(), null);
                if (customerByEmail == null) {
                    customerByEmail = this.customersRepository.findByUserName(loginDto.getEmail(), null);
                }
                if (customerByEmail == null) {
                    resBody.put("error", "Member not found");
                    return resBody;
                }
                if (customerByEmail.getPassword() == null || customerByEmail.getPassword().isBlank()) {
                    resBody.put("error", "Password not set for this user. Please contact administrator.");
                    return resBody;
                }
                if (customerByEmail.getLoginPreference() != null) {
                    // resBody.put("loginPreference", customerByEmail.getLoginPreference());1
                    resBody.put("loginPreference", "password");
                    return resBody;
                } else {
                    resBody.put("loginPreference", "password");
                    return resBody;
                }
            }
            Customers customer = this.customersRepository.findByEmailAndPassword(loginDto.getEmail(),
                    loginDto.getPassword());
            if (customer == null) {
                customer = this.customersRepository.findByUserNameAndPassword(loginDto.getEmail(),
                        loginDto.getPassword());
            }
            if (customer != null) {
                CustomersDto customerDto = new CustomersDto();
                BeanUtils.copyProperties(customer, customerDto);
                customerDto.setPassword(null);
                final String jwtToken = jwtUtil.generateToken(customer);
                resBody.put("token", jwtToken);
                resBody.put("userId", customer.getId());
                resBody.put("username", customer.getUsername());
                resBody.put("name", customer.getFirstName() + " " + customer.getLastName());
                resBody.put("email", customer.getEmailAddress());
                resBody.put("subUser", customer.getCustomers() != null);
                resBody.put("webConference", customer.getWebConference() != null);
                resBody.put("timeZone", customer.getTimeZone() != null);
                if (customer.getRole() != null && customer.getCustomers() == null) {
                    resBody.put("roleId", customer.getRole().getId());
                    resBody.put("roleName", customer.getRole().getRole());
                } else {
                    resBody.put("roleId", customer.getSubUserType().getId());
                    resBody.put("roleName", customer.getSubUserType().getName());
                    resBody.put("permissions",
                            this.subUserTypeService.getSubUserTypeById(customer.getSubUserType().getId()));
                }
            } else {
                resBody.put("error", "Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", "Login failed due to internal error.");
        }
        return resBody;
    }

    @Override
    public Map<String, Object> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        try {
            Map<String, Object> res = new HashMap<>();

            if (forgotPasswordDto.getUsername() == null || forgotPasswordDto.getUsername().isBlank()) {
                throw new RuntimeException("Username is required");
            }

            if (forgotPasswordDto.getQuestion() != null && forgotPasswordDto.getAnswer() != null) {
                Customers customer = this.customersRepository.findByUserName(forgotPasswordDto.getUsername(), null);
                String correctAnswer = null;
                switch (forgotPasswordDto.getQuestionId()) {
                    case 1 -> correctAnswer = customer.getAnswer1();
                    case 2 -> correctAnswer = customer.getAnswer2();
                    case 3 -> correctAnswer = customer.getAnswer3();
                    default -> throw new RuntimeException("Invalid question id");
                }
                if (customer == null) {
                    throw new RuntimeException("Account not found with username: " + forgotPasswordDto.getUsername());
                }
                if (correctAnswer != null && correctAnswer.equals(forgotPasswordDto.getAnswer())) {
                    if (this.generateToken(Long.parseLong(customer.getId().toString()), customer.getUsername(),
                            customer.getEmailAddress())) {
                        res.put("isAnswerCorrect", true);
                        res.put("message", "A password reset link has been sent to your registered email address.");
                        return res;
                    }
                } else {
                    res.put("isAnswerCorrect", false);
                    res.put("message", "The answer provided is incorrect. Please try again.");
                    return res;
                }
            }

            Customers customer = this.customersRepository.findByUserName(forgotPasswordDto.getUsername(), null);
            if (customer == null) {
                throw new RuntimeException("Account not found with username " + forgotPasswordDto.getUsername());
            }

            int randomNum = random.nextInt(3) + 1;

            String question = null;
            switch (randomNum) {
                case 1 -> question = customer.getQuestion1();
                case 2 -> question = customer.getQuestion2();
                case 3 -> question = customer.getQuestion3();
            }
            res.put("questionId", randomNum);
            res.put("question", question);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> resetPassword(ResetPasswordDto resetPasswordDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(resetPasswordDto.getToken()));
            String[] parts = decodedToken.split(":");

            if (parts.length != 4) { // Expecting id, UUID, timestamp, and HMAC
                resBody.put("message", "Invalid token structure");
                resBody.put("status", 400);
                return resBody; // Token structure is invalid
            }
            String id = parts[0];
            Customers customer = this.customersRepository.findById(Integer.parseInt(id))
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            customer.setPassword(resetPasswordDto.getPassword());
            this.customersRepository.save(customer);
            resBody.put("success", "Password has been reset successfully.");
            return resBody;
        } catch (Exception e) {
            throw new RuntimeException("Error resetPassword: " + e.getMessage(), e);
        }
    }

    private boolean generateTokenForSubUserRegister(Long id, String name, String email) throws Exception {
        int currentYear = java.time.Year.now().getValue();
        Customers customers = this.customersRepository.findById(Integer.parseInt(id.toString()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        customers.setUsername("");
        customers.setPassword("");
        this.customersRepository.save(customers);

        long currentTimestamp = System.currentTimeMillis();
        String data = id + ":" + UUID.randomUUID().toString() + ":" + currentTimestamp;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());

        String token = data + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        String link = Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes());

        String route = siteUrl + "subaccountactivesetup/" + link;
        String subject = "Activate Your 360Pipe Account";

        // HTML email body with header, footer, and button
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Activate Your Account</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { text-align: center; padding: 20px 0; border-bottom: 2px solid #eee; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 20px 0; }" +
                ".button {" +
                "  display: inline-block;" +
                "  padding: 12px 24px;" +
                "  background-color: #44288E;" +
                "  color: #ffffff !important;" +
                "  text-decoration: none;" +
                "  border-radius: 4px;" +
                "  font-weight: bold;" +
                "}" +
                ".button:hover { background-color: #44288E; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.9em; color: #777; text-align: center; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<img src=\"" + siteUrl + "images/logo/360Pipe_logo.png\" alt=\"360Pipe Logo\" class=\"logo\">" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Hi " + name + ",</p>" +
                "<p>You’ve been invited to 360Pipe.</p>" +
                "<p>360Pipe helps your team track deal progression,  priorities, and pipeline activity, all while automatically keeping Salesforce up to date.</p>"
                +
                "<p>Please click the link below to set your password and activate your account:</p>" +
                "<p><a href=\"" + route + "\" class=\"button\">Set Your Password</a></p>" +
                "<p>If you need assistance, please contact 360Pipe Support at <a href=\"mailto:360pipeinc@gmail.com\">360pipeinc@gmail.com</a>.</p>"
                +
                "<p>Welcome to 360Pipe.</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>&copy; " + currentYear + " 360Pipe. All rights reserved." +
                "</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Send as HTML
        return this.commonService.sendEmail(email, subject, body, true);
    }

    private boolean generateToken(Long id, String name, String email) throws Exception {
        int currentYear = java.time.Year.now().getValue();
        long currentTimestamp = System.currentTimeMillis();
        String data = id + ":" + UUID.randomUUID().toString() + ":" + currentTimestamp;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());

        String token = data + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        String link = Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes());

        String route = siteUrl + "resetpassword/" + link;
        String subject = "Reset Your 360Pipe Password";

        // HTML email body with header, footer, and button
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Reset Your Password</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { text-align: center; padding: 20px 0; border-bottom: 2px solid #eee; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 20px 0; }" +
                ".button {" +
                "  display: inline-block;" +
                "  padding: 12px 24px;" +
                "  background-color: #44288E;" +
                "  color: #ffffff !important;" +
                "  text-decoration: none;" +
                "  border-radius: 4px;" +
                "  font-weight: bold;" +
                "}" +
                ".button:hover { background-color: #44288E; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.9em; color: #777; text-align: center; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<img src=\"" + siteUrl + "images/logo/360Pipe_logo.png\" alt=\"360Pipe Logo\" class=\"logo\">" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Hi " + name + ",</p>" +
                "<p>We received a request to reset your 360Pipe password.</p>" +
                "<p>Click the link below to create a new password:</p>" +
                "<p><a href=\"" + route + "\" class=\"button\">Reset Password</a></p>" +
                "<p>If you did not request this change, you can safely ignore this email.</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>&copy; " + currentYear + " 360Pipe. All rights reserved" +
                "</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // Send as HTML
        return this.commonService.sendEmail(email, subject, body, true);
    }

    @Override
    public Map<String, Object> validateSubUserToken(String token) {
        Map<String, Object> res = new HashMap<>();
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(token));
            String[] parts = decodedToken.split(":");
            if (parts.length != 4) {
                res.put("message", "Invalid token structure");
                res.put("status", 400);
                return res;
            }

            String id = parts[0];
            String uuid = parts[1];
            long timestamp = Long.parseLong(parts[2]);
            String providedHmac = parts[3];

            Customers user = this.customersRepository.findById(Integer.parseInt(id))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Use property for 24 hours
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - timestamp > subUserTokenExpiration) {
                res.put("message", "Token is expired.");
                res.put("status", 404);
                return res;
            }

            String data = id + ":" + uuid + ":" + timestamp;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] expectedHmac = mac.doFinal(data.getBytes());

            if (MessageDigest.isEqual(expectedHmac, Base64.getUrlDecoder().decode(providedHmac))) {
                res.put("message", "Token is valid");
                res.put("status", 200);
                res.put("userId", Integer.parseInt(id));
                res.put("email", user.getEmailAddress());
                return res;
            }
            res.put("message", "Invalid token signature");
            res.put("status", 400);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error validating token: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> res = new HashMap<>();
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(token));
            String[] parts = decodedToken.split(":");
            if (parts.length != 4) {
                res.put("message", "Invalid token structure");
                res.put("status", 400);
                return res;
            }

            String id = parts[0];
            String uuid = parts[1];
            long timestamp = Long.parseLong(parts[2]);
            String providedHmac = parts[3];

            Customers user = this.customersRepository.findById(Integer.parseInt(id))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Use property for 15 minutes
            long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp - timestamp > resetTokenExpiration) {
                res.put("message", "Token is expired.");
                res.put("status", 404);
                return res;
            }

            String data = id + ":" + uuid + ":" + timestamp;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] expectedHmac = mac.doFinal(data.getBytes());

            if (MessageDigest.isEqual(expectedHmac, Base64.getUrlDecoder().decode(providedHmac))) {
                res.put("message", "Token is valid");
                res.put("status", 200);
                res.put("userId", Integer.parseInt(id));
                return res;
            }
            res.put("message", "Invalid token signature");
            res.put("status", 400);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error validating token: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> changePassword(Map<String, Object> data) {
        try {
            Map<String, Object> res = new HashMap<>();
            Customers customer = this.customersRepository.findById(Integer.parseInt(data.get("userId").toString()))
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            if (!customer.getPassword().equals(data.get("oldPassword").toString())) {
                res.put("error", "Old Password Not Matched");
            }
            customer.setPassword(data.get("newPassword").toString());
            this.customersRepository.save(customer);
            res.put("success", "Password changed successfully");
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean sendRegisterInvitation(Map<String, Object> data) {
        try {
            return this.generateTokenForSubUserRegister(Long.parseLong(data.get("userId").toString()),
                    data.get("name").toString(), data.get("email").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CustomerDashboardDto getManagerDashboardData(Integer customerId, CustomerDashboardRequestDto req) {
        try {
            CustomerDashboardDto dto = new CustomerDashboardDto();

            Customers manager = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            List<Customers> subs = this.customersRepository.getAllSubUsersWithRepRole(manager.getId());
            Set<Integer> allCustomerIds = new HashSet<>();
            allCustomerIds.add(manager.getId());
            if (subs != null && !subs.isEmpty()) {
                for (Customers c : subs) {
                    allCustomerIds.add(c.getId());
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            Timestamp startTs = null;
            Timestamp endTs = null;

            if (req != null && req.getStartDate() != null && req.getEndDate() != null
                    && !req.getStartDate().isBlank() && !req.getEndDate().isBlank()) {
                LocalDate start = LocalDate.parse(req.getStartDate().trim(), formatter);
                LocalDate end = LocalDate.parse(req.getEndDate().trim(), formatter);
                startTs = Timestamp.valueOf(start.atStartOfDay());
                endTs = Timestamp.valueOf(end.plusDays(1).atStartOfDay().minusNanos(1));
            }

            int totalContacts = 0;
            int totalMeetings = 0;
            int totalNewMeetings = 0;
            int totalOldMeetings = 0;
            Integer totalDealAmount = 0;
            Integer totalClosedDealAmount = 0;
            Integer totalPipeLineAmount = 0;

            // Using a Map to group by Account Name
            Map<String, Map<String, Object>> groupedPipeLineData = new LinkedHashMap<>();
            List<Map<String, Object>> meetingData = new ArrayList<>();

            for (Integer cid : allCustomerIds) {
                Integer contactCountForCustomer;
                Integer meetingCountForCustomer;
                Integer newMeetingCountForCustomer = 0;
                Integer oldMeetingCountForCustomer = 0;

                List<Opportunities> closedOpp;
                List<Opportunities> opportunities;
                List<Opportunities> allPipeLineOpportunities;

                if (startTs != null && endTs != null) {
                    contactCountForCustomer = this.contactsRepository.countContactsByCustomerAndDateRange(cid, startTs,
                            endTs);
                    List<Meetings> meetings = this.meetingsRepository.countMeetingsByCustomerAndDateRange(cid, startTs,
                            endTs);
                    meetingCountForCustomer = (meetings != null) ? meetings.size() : 0;

                    if (meetings != null && !meetings.isEmpty()) {
                        for (Meetings meeting : meetings) {
                            if (meeting.getContactIds() != null && !meeting.getContactIds().isEmpty()) {
                                Integer[] contactIds = parseContactIds(meeting.getContactIds());
                                for (Integer contactId : contactIds) {
                                    if (contactId == null)
                                        continue;
                                    Contacts contacts = this.contactsRepository.findById(contactId)
                                            .orElseThrow(() -> new RuntimeException("Contact not found"));
                                    Map<String, Object> meetingObj = new HashMap<>();
                                    meetingObj.put("id", contacts.getAccount().getId());
                                    meetingObj.put("account_name", contacts.getAccount().getAccountName());
                                    meetingData.add(meetingObj);
                                    if (Boolean.TRUE.equals(contacts.getFromMailScraping()))
                                        newMeetingCountForCustomer++;
                                    else
                                        oldMeetingCountForCustomer++;
                                }
                            }
                        }
                    }
                    closedOpp = this.opportunitiesRepository.findClosedOpportunitiesByCustomerAndDateRange(cid, startTs,
                            endTs);
                    opportunities = this.opportunitiesRepository.findByCustomerAndDateRange(cid, startTs, endTs);
                    allPipeLineOpportunities = this.opportunitiesRepository
                            .findActiveOpportunitiesByCustomerAndDateRange(cid, startTs, endTs);
                } else {
                    contactCountForCustomer = this.contactsRepository.countNewContactByCustomerId(cid);
                    meetingCountForCustomer = this.meetingsRepository.countMeetingByCustomerId(cid);
                    closedOpp = this.opportunitiesRepository.closeDealOpportunitiesByCustomerId(cid);
                    opportunities = this.opportunitiesRepository.findActiveOpportunities(cid);
                    allPipeLineOpportunities = this.opportunitiesRepository.findActiveOpportunities(cid);
                }

                totalContacts += (contactCountForCustomer != null ? contactCountForCustomer : 0);
                totalMeetings += (meetingCountForCustomer != null ? meetingCountForCustomer : 0);
                totalNewMeetings += newMeetingCountForCustomer;
                totalOldMeetings += oldMeetingCountForCustomer;

                if (closedOpp != null) {
                    for (Opportunities opp : closedOpp) {
                        if (opp.getDealAmount() != null)
                            totalClosedDealAmount += Integer.parseInt(opp.getDealAmount().toString());
                    }
                }

                if (opportunities != null) {
                    for (Opportunities opp : opportunities) {
                        if (opp.getDealAmount() != null && "Pipeline".equals(opp.getStatus())) {
                            totalDealAmount += Integer.parseInt(opp.getDealAmount().toString());
                        }
                    }
                }

                if (allPipeLineOpportunities != null) {
                    for (Opportunities opp : allPipeLineOpportunities) {
                        if (opp.getDealAmount() != null)
                            totalPipeLineAmount += Integer.parseInt(opp.getDealAmount().toString());

                        if ("Pipeline".equals(opp.getStatus())) {
                            String accountName = (opp.getAccount() != null) ? opp.getAccount().getAccountName()
                                    : "Unknown Account";
                            int currentDealAmount = Integer.parseInt(opp.getDealAmount().toString());

                            // Grouping Logic
                            if (!groupedPipeLineData.containsKey(accountName)) {
                                Map<String, Object> accountGroup = new HashMap<>();
                                accountGroup.put("id", opp.getId());
                                accountGroup.put("account", accountName);
                                accountGroup.put("created_by",
                                        opp.getCustomers() != null ? opp.getCustomers().getUsername() : null);
                                accountGroup.put("totalDealAmount", 0);
                                accountGroup.put("opps", new ArrayList<Map<String, Object>>());
                                groupedPipeLineData.put(accountName, accountGroup);
                            }

                            Map<String, Object> group = groupedPipeLineData.get(accountName);

                            // Update Total Deal Amount for this Account
                            int currentTotal = (int) group.get("totalDealAmount");
                            group.put("totalDealAmount", currentTotal + currentDealAmount);

                            // Add this specific opportunity to the 'opps' list
                            List<Map<String, Object>> oppsList = (List<Map<String, Object>>) group.get("opps");
                            Map<String, Object> oppDetails = new HashMap<>();
                            oppDetails.put("name", opp.getOpportunity());
                            oppDetails.put("dealAmount", currentDealAmount);
                            oppsList.add(oppDetails);
                        }
                    }
                }
            }

            dto.setCustomerId(customerId);
            dto.setTotalContacts(totalContacts);
            dto.setTotalMeetings(totalMeetings);
            dto.setTotalNewMeetings(totalNewMeetings);
            dto.setTotalOldMeetings(totalOldMeetings);
            dto.setTotalClosedDealAmount(totalClosedDealAmount);
            dto.setTotalDealAmount(totalDealAmount);
            dto.setTotalPipeLine(totalPipeLineAmount);

            // Convert Map values back to a List for the DTO
            dto.setPipeLineData(new ArrayList<>(groupedPipeLineData.values()));
            dto.setMeetingData(meetingData);

            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Integer[] parseContactIds(String contactIdsStr) {
        if (contactIdsStr == null)
            return new Integer[0];

        String cleaned = contactIdsStr.trim();
        cleaned = cleaned.replace("[", "").replace("]", "").trim();

        if (cleaned.isEmpty())
            return new Integer[0];

        return Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.valueOf(s);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Integer[]::new);
    }

    private void sendWelcomeEmail(String email, String name) {
        int currentYear = java.time.Year.now().getValue();
        String route = siteUrl + "login";
        String subject = "Welcome to 360Pipe!";

        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Welcome to 360Pipe</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { text-align: center; padding: 20px 0; border-bottom: 2px solid #eee; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 20px 0; }" +
                ".button {" +
                "  display: inline-block;" +
                "  padding: 12px 24px;" +
                "  background-color: #44288E;" +
                "  color: #ffffff !important;" + // Added !important to ensure visibility in all clients
                "  text-decoration: none;" +
                "  border-radius: 4px;" +
                "  font-weight: bold;" +
                "}" +
                ".button:hover { background-color: #44288E; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.9em; color: #777; text-align: center; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<img src=\"" + siteUrl + "images/logo/360Pipe_logo.png\" alt=\"360Pipe Logo\" class=\"logo\">" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Hi " + name + ",</p>" +
                "<p>Welcome to 360Pipe! We’re excited to have you on board.</p>" +
                "<p>Your account is now fully active. You can now start tracking your deal progression and keeping your Salesforce data synchronized seamlessly.</p>"
                +
                "<p>Click the button below to log in and explore your dashboard:</p>" +
                "<p><a href=\"" + route + "\" class=\"button\">Login</a></p>" +
                "<p>If you need assistance, please contact 360Pipe Support at <a href=\"mailto:360pipeinc@gmail.com\">360pipeinc@gmail.com</a>.</p>"
                +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>&copy; " + currentYear + " 360Pipe. All rights reserved." +
                "</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        this.commonService.sendEmail(email, subject, body, true);
    }

    @Override
    public void saveCustomerTimeZone(String timeZone, Integer customerId) {
        try {
            Customers customers = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));
            customers.setTimeZone(timeZone);
            this.customersRepository.save(customers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveWebConference(String webConference, Integer customerId) {
        try {
            Customers customers = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));
            customers.setWebConference(webConference);
            this.customersRepository.save(customers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveMailNotification(String mailNotification, Integer customerId) {
        try {
            Customers customers = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));
            customers.setEmailNotification(mailNotification);
            this.customersRepository.save(customers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDefaultCalendar(String defaultCalendar, Integer customerId) {
        try {
            Customers customers = this.customersRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));
            customers.setDefaultCalendar(defaultCalendar);
            this.customersRepository.save(customers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> reportHierarch(Integer customerId) {
        try {
            Customers start = customersRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

            // Keep hierarchy inside the same parent customer if applicable
            Integer orgId = (start.getCustomers() != null) ? start.getCustomers().getId() : start.getId();

            // 1) Build upward chain: start -> reportTo -> ... -> top (cycle-safe)
            List<Customers> upward = collectUpwardChain(start);

            // upward is [start, reportTo, grandReportTo, ... top]
            // root should be the TOP
            Collections.reverse(upward); // [top ... reportTo start]

            // 2) We want a path list of IDs: [topId, ..., startId]
            List<Integer> pathIds = upward.stream()
                    .map(Customers::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (pathIds.isEmpty()) {
                // extremely defensive fallback
                return toMapLite(start, null);
            }

            // 3) Build a tree from the top, expanding siblings at each manager level.
            // Only expand children deeper for the node on the path; other siblings are leaf
            // nodes.
            Set<Integer> buildVisited = new HashSet<>();
            CustomerNodeDto root = buildHierarchyAlongPath(
                    pathIds.get(0),
                    orgId,
                    pathIds,
                    1,
                    buildVisited);

            // 4) Serialize
            return toMap(root);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<Customers> collectUpwardChain(Customers start) {
        List<Customers> chain = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        Customers cur = start;
        while (cur != null) {
            Integer id = cur.getId();
            if (id != null && !visited.add(id)) {
                // cycle detected -> stop
                break;
            }
            chain.add(cur);
            cur = cur.getReportTo(); // manager ("report_to")
        }
        return chain;
    }

    private CustomerNodeDto buildHierarchyAlongPath(
            Integer currentId,
            Integer orgId,
            List<Integer> pathIds,
            int nextPathIndex,
            Set<Integer> buildVisited) {
        if (currentId == null)
            return null;

        // Cycle protection during build
        if (!buildVisited.add(currentId)) {
            CustomerNodeDto cycleNode = new CustomerNodeDto();
            Customers c = customersRepository.findById(currentId).orElse(null);
            cycleNode.id = currentId;
            cycleNode.name = (c != null) ? fullName(c) : "Unknown";
            cycleNode.title = (c != null) ? c.getTitle() : null;
            cycleNode.children = null; // stop expansion
            return cycleNode;
        }

        Customers current = customersRepository.findById(currentId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + currentId));

        CustomerNodeDto node = new CustomerNodeDto();
        node.id = current.getId();
        node.name = fullName(current);
        node.title = current.getTitle();

        // Direct reports (siblings group at this level)
        List<Customers> reports = customersRepository.findDirectReports(orgId, currentId);

        // Sort for stable UI (optional)
        reports.sort(Comparator.comparing(this::fullName, String.CASE_INSENSITIVE_ORDER));

        if (reports.isEmpty()) {
            node.children = null;
            return node;
        }

        Integer nextOnPath = (nextPathIndex < pathIds.size()) ? pathIds.get(nextPathIndex) : null;

        List<CustomerNodeDto> childDtos = new ArrayList<>(reports.size());
        for (Customers r : reports) {
            if (nextOnPath == null) {
                // At or below the selected node -> fully expand all descendants
                childDtos.add(buildHierarchyAlongPath(r.getId(), orgId, pathIds, nextPathIndex + 1, buildVisited));
            } else if (r.getId() != null && r.getId().equals(nextOnPath)) {
                // Above the selected node -> expand only the path leading to it
                childDtos.add(buildHierarchyAlongPath(r.getId(), orgId, pathIds, nextPathIndex + 1, buildVisited));
            } else {
                // Sibling at a higher level (not on path to the selected node) -> leaf
                CustomerNodeDto leaf = new CustomerNodeDto();
                leaf.id = r.getId();
                leaf.name = fullName(r);
                leaf.title = r.getTitle();
                leaf.children = null;
                childDtos.add(leaf);
            }
        }

        node.children = childDtos.isEmpty() ? null : childDtos;
        return node;
    }

    private String fullName(Customers c) {
        String fn = c.getFirstName();
        String ln = c.getLastName();
        String name = ((fn == null ? "" : fn.trim()) + " " + (ln == null ? "" : ln.trim())).trim();
        if (name.isEmpty())
            name = "Unknown";
        return name;
    }

    private Map<String, Object> toMap(CustomerNodeDto node) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", node.id);
        m.put("name", node.name);
        m.put("title", node.title);

        if (node.children == null || node.children.isEmpty()) {
            m.put("children", null);
        } else {
            List<Map<String, Object>> kids = new ArrayList<>(node.children.size());
            for (CustomerNodeDto ch : node.children)
                kids.add(toMap(ch));
            m.put("children", kids);
        }
        return m;
    }

    private Map<String, Object> toMapLite(Customers c, List<Map<String, Object>> children) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", fullName(c));
        m.put("title", c.getTitle());
        m.put("children", children == null || children.isEmpty() ? null : children);
        return m;
    }

}
