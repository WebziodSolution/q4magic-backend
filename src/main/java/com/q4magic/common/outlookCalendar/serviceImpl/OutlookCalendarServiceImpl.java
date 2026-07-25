package com.q4magic.common.outlookCalendar.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.q4magic.common.dto.LocationResultDto;
import com.q4magic.common.dto.outlookCalendar.OutlookCalendarDto;
import com.q4magic.common.locationExtractorService.LocationExtractorService;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.models.CalendarDetails;
import com.q4magic.common.models.Customers;
import com.q4magic.common.outlookCalendar.service.OutlookCalendarService;
import com.q4magic.common.repository.CalendarDetailsRepository;
import com.q4magic.common.repository.CalendarRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(value = "OutlookCalendarService")
public class OutlookCalendarServiceImpl implements OutlookCalendarService {
    @Value("${server.database.timezone}")
    String serverDatabaseTimeZone;

    @Value("${outlook.client.clientId}")
    private String clientId;
    @Value("${outlook.client.clientSecret}")
    private String clientSecret;
    @Value("${outlook.client.scope}")
    private String scope;
    @Value("${outlook.client.redirectUri}")
    private String redirectURI;
    @Value("${outlook.client.calendarApiUri}")
    private String calendarApiUri;
    @Value("${outlook.client.calendarAuthorizeApiUri}")
    private String calendarAuthorizeApiUri;
    @Autowired
    private LocationExtractorService locationService;
    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CalendarDetailsRepository calendarDetailsRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public String authorize() throws UnsupportedEncodingException {
        String requestUrl = calendarAuthorizeApiUri + "authorize?" +
                "client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectURI +
                "&response_mode=query" +
                "&scope=" + URLEncoder.encode(scope, "UTF-8") +
                "&state=12345" +
                "&prompt=consent";
        return requestUrl;
    }

    @Override
    public String refreshToken(String outlookCalendarRefreshToken, Customers customers) {
        try {
            String requestUrl = calendarAuthorizeApiUri + "token";
            RestTemplate apiCall = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<String>("client_id=" + clientId + "&redirect_uri=" + redirectURI
                    + "&client_secret=" + clientSecret + "&scope=" + scope + "&refresh_token="
                    + outlookCalendarRefreshToken + "&grant_type=refresh_token", headers);
            String message = apiCall.postForObject(requestUrl, entity, String.class);
            // log.error("Response : "+ message);
            JSONObject jsonObject = new JSONObject(message);

            customers.setOutlookCalendarAccessToken(jsonObject.getString("access_token"));
            if (jsonObject.has("refresh_token")) {
                customers.setOutlookCalendarRefreshToken(jsonObject.getString("refresh_token"));
            }
            this.customersRepository.save(customers);

            return jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public String getUserCalendarTimezone(String accessToken) {
        try {
            RestTemplate apiCall = new RestTemplate();
            String requestUrl = "https://graph.microsoft.com/v1.0/me/mailboxsettings/timeZone";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
            JSONObject response = new JSONObject(responseJson.getBody());
            return response.getString("value");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public void saveToken(Integer cusId, String code) {
        try {
            String requestUrl = calendarAuthorizeApiUri + "token";
            RestTemplate apiCall = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<String>(
                    "client_id=" + clientId + "&redirect_uri=" + redirectURI + "&client_secret=" + clientSecret
                            + "&code=" + code + "&scope=" + scope + "&grant_type=authorization_code",
                    headers);
            String message = apiCall.postForObject(requestUrl, entity, String.class);
            // log.error("Response : "+ message);
            JSONObject jsonObject = new JSONObject(message);

            Customers customer = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            customer.setOutlookCalendarAccessToken(jsonObject.getString("access_token"));
            if (jsonObject.has("refresh_token")) {
                customer.setOutlookCalendarRefreshToken(jsonObject.getString("refresh_token"));
            }
            this.customersRepository.save(customer);
            this.getEmail(cusId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getEmail(Integer cusId) {
        try {
            Customers customers = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            String accessToken = customers.getOutlookCalendarAccessToken();
            if (accessToken == null) {
                accessToken = "";
            }
            if (!accessToken.equals("")) {
                accessToken = refreshToken(customers.getOutlookCalendarRefreshToken(), customers);
                if (accessToken != null) {
                    RestTemplate apiCall = new RestTemplate();
                    String requestUrl = "https://graph.microsoft.com/v1.0/me";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + accessToken);
                    HttpEntity<String> entity = new HttpEntity<String>("", headers);
                    ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity,
                            String.class);
                    JSONObject response = new JSONObject(responseJson.getBody());
                    String outlookCalendarEmail = response.getString("mail").toString();
                    customers.setOutlookCalendarEmail(outlookCalendarEmail);
                    this.customersRepository.save(customers);
                    return outlookCalendarEmail;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public Map<String, Object> saveEvent(Integer cusId, OutlookCalendarDto outlookCalendarDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");

        try {
            Customers customers = customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            String accessToken = customers.getOutlookCalendarAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                // If no access token, try to refresh using refresh token
                accessToken = refreshToken(customers.getOutlookCalendarRefreshToken(), customers);
                if (accessToken == null) {
                    resBody.put("error", "Unable to obtain access token");
                    return resBody;
                }
            }

            String timeZone = getUserCalendarTimezone(accessToken);
            if (outlookCalendarDto.getCalTimeZone() != null && !outlookCalendarDto.getCalTimeZone().isEmpty()) {
                timeZone = outlookCalendarDto.getCalTimeZone();
            }

            RestTemplate apiCall = new RestTemplate();
            String requestUrl = calendarApiUri + "events";
            boolean isUpdate = outlookCalendarDto.getCaldSycId() != null
                    && outlookCalendarDto.getCaldSycId().length() > 0;
            if (isUpdate) {
                requestUrl += "/" + outlookCalendarDto.getCaldSycId();
            }

            // ---- Build JSON payload using Jackson ----
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            // Subject
            root.put("subject", outlookCalendarDto.getCalTitle());

            // Body (only if description is not null/empty)
            String description = outlookCalendarDto.getCalDescription();
            if (description != null && !description.trim().isEmpty()) {
                ObjectNode bodyNode = mapper.createObjectNode();
                bodyNode.put("contentType", "HTML");
                // nl2br converts newlines to <br> – keep as is
                bodyNode.put("content", commonService.nl2br(description));
                root.set("body", bodyNode);
            }

            // All-day flag
            boolean isAllDay = Boolean.parseBoolean(outlookCalendarDto.getCalAllDay());
            root.put("isAllDay", isAllDay);

            // Attendees (if any)
            if (outlookCalendarDto.getCalAttendees() != null && !outlookCalendarDto.getCalAttendees().isEmpty()) {
                ArrayNode attendeesArray = mapper.createArrayNode();
                for (String attendeeEmail : outlookCalendarDto.getCalAttendees()) {
                    if (attendeeEmail != null && !attendeeEmail.trim().isEmpty()) {
                        ObjectNode attendeeNode = mapper.createObjectNode();
                        ObjectNode emailNode = mapper.createObjectNode();
                        emailNode.put("address", attendeeEmail);
                        emailNode.put("name", attendeeEmail); // or use outlookCalendarDto.getCalTitle()
                        attendeeNode.set("emailAddress", emailNode);
                        attendeeNode.put("type", "required");
                        attendeesArray.add(attendeeNode);
                    }
                }
                if (attendeesArray.size() > 0) {
                    root.set("attendees", attendeesArray);
                }
            }

            // Start & End date/time handling
            String rawStart = outlookCalendarDto.getCalStartDateTime();
            String rawEnd = outlookCalendarDto.getCalEndDateTime();
            String startDateTime, endDateTime;

            if (isAllDay) {
                // For all-day events, Graph expects date-only in yyyy-MM-dd
                DateTimeFormatter inputFormatter = DateTimeFormatter
                        .ofPattern("[MM/dd/yyyy HH:mm:ss][yyyy-MM-dd HH:mm:ss][MM/dd/yyyy][yyyy-MM-dd]");
                LocalDate startDate = LocalDate.parse(rawStart, inputFormatter);
                LocalDate endDate = LocalDate.parse(rawEnd, inputFormatter);

                // Ensure end date is strictly after start date (Outlook requirement)
                if (!endDate.isAfter(startDate)) {
                    endDate = startDate.plusDays(1);
                }

                startDateTime = startDate.toString(); // 2026-05-08
                endDateTime = endDate.toString(); // 2026-05-09
            } else {
                // Non-all-day events: keep original date+time
                startDateTime = rawStart;
                endDateTime = rawEnd;
            }

            // Start object
            ObjectNode startNode = mapper.createObjectNode();
            startNode.put("dateTime", startDateTime);
            startNode.put("timeZone", timeZone);
            root.set("start", startNode);

            // End object
            ObjectNode endNode = mapper.createObjectNode();
            endNode.put("dateTime", endDateTime);
            endNode.put("timeZone", timeZone);
            root.set("end", endNode);

            String requestJson = mapper.writeValueAsString(root);

            // ---- Execute the request ----
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> responseJson;
            if (isUpdate) {
                // Use PATCH for update
                RestTemplate restTemplate = new RestTemplate();
                // Optional: configure HttpClient if needed
                restTemplate.setRequestFactory(
                        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build()));
                responseJson = restTemplate.exchange(requestUrl, HttpMethod.PATCH, entity, String.class);
            } else {
                // Use POST for creation
                responseJson = apiCall.exchange(requestUrl, HttpMethod.POST, entity, String.class);
            }

            JSONObject jsonObject = new JSONObject(responseJson.getBody());
            resBody.put("caldSycId", jsonObject.getString("id"));

        } catch (HttpClientErrorException e) {
            // Log the actual response body for debugging
            String errorBody = e.getResponseBodyAsString();
            resBody.put("error", "HTTP " + e.getRawStatusCode() + ": " + errorBody);
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
            throw new RuntimeException(e);
        }
        return resBody;
    }
    // public Map<String, Object> saveEvent(Integer cusId, OutlookCalendarDto
    // outlookCalendarDto) {
    // Map<String, Object> resBody = new HashMap<>();
    // resBody.put("error", "");
    // try {
    // Customers customers = this.customersRepository.findById(cusId).orElseThrow(()
    // -> new RuntimeException("Customer not found"));
    // String accessToken = customers.getOutlookCalendarAccessToken();
    // if (accessToken == null) {
    // accessToken = "";
    // }
    // if (!accessToken.isEmpty()) {
    // accessToken = refreshToken(customers.getOutlookCalendarRefreshToken(),
    // customers);
    // if (accessToken != null) {
    // String timeZone = getUserCalendarTimezone(accessToken);
    //
    // RestTemplate apiCall = new RestTemplate();
    // String requestUrl = calendarApiUri + "events";
    // if (outlookCalendarDto.getCaldSycId().length() > 0) {
    // requestUrl += "/" + outlookCalendarDto.getCaldSycId();
    // }
    //
    // String requestJson = "{";
    // requestJson += "\"subject\":\"" + outlookCalendarDto.getCalTitle() + "\"";
    // requestJson += ",\"body\":";
    // if (outlookCalendarDto.getCalDescription() != null) {
    // requestJson += "{\"contentType\":\"HTML\"";
    // requestJson += ",\"content\":\"" +
    // this.commonService.nl2br(outlookCalendarDto.getCalDescription()) + "\"}";
    // }
    // requestJson += ",\"isAllDay\":" +
    // Boolean.parseBoolean(outlookCalendarDto.getCalAllDay()) + "";
    // String calStartDateTime = outlookCalendarDto.getCalStartDateTime();
    // String calEndDateTime = outlookCalendarDto.getCalEndDateTime();
    //
    // try {
    // if (outlookCalendarDto.getCalAttendees() != null) {
    // if (outlookCalendarDto.getCalAttendees().stream().count() > 0) {
    // requestJson += ",\"attendees\":[";
    // int count = 0;
    // for (String attendees : outlookCalendarDto.getCalAttendees()) {
    // if (count > 0) {
    // requestJson += ",";
    // }
    //
    //// requestJson += "{\"emailAddress\":{\"address\":\"" + attendees +
    // "\",\"name\":\"" + outlookCalendarDto.getCalTitle() +
    // "\"},\"type\":\"required\"}";
    //
    // requestJson += "{\"emailAddress\":{\"address\":\"" + attendees +
    // "\",\"name\":\"" + attendees + "\"},\"type\":\"required\"}";
    // count++;
    // }
    // requestJson += "]";
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // if (!outlookCalendarDto.getCalTimeZone().equals("") &&
    // outlookCalendarDto.getCalTimeZone() != null) {
    // //if (outlookCalendarDto.getCalTimeZone() != null) {
    // timeZone = outlookCalendarDto.getCalTimeZone();
    // //}
    // }
    // // 4. All-Day Logic (+1 Day logic added here)
    // boolean isAllDayBool =
    // Boolean.parseBoolean(outlookCalendarDto.getCalAllDay());
    // if (isAllDayBool) {
    // try {
    // DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("[MM/dd/yyyy
    // HH:mm:ss][yyyy-MM-dd HH:mm:ss]");
    //
    // LocalDateTime startObj = LocalDateTime.parse(calStartDateTime,
    // inputFormatter)
    // .withHour(0).withMinute(0).withSecond(0).withNano(0);
    // LocalDateTime endObj = LocalDateTime.parse(calEndDateTime, inputFormatter)
    // .withHour(0).withMinute(0).withSecond(0).withNano(0);
    //
    // // Java Logic: If end is same or before start, add 1 day (Outlook
    // requirement)
    // if (!endObj.isAfter(startObj)) {
    // endObj = startObj.plusDays(1);
    // }
    //
    // // Format back for the API
    // DateTimeFormatter outlookFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy
    // 00:00:00");
    // calStartDateTime = startObj.format(outlookFormatter);
    // calEndDateTime = endObj.format(outlookFormatter);
    //
    // } catch (DateTimeParseException e) {
    // e.printStackTrace();
    // throw new RuntimeException(e.getMessage());
    // }
    // }
    //
    //
    // requestJson += ",\"start\":";
    // requestJson += "{\"dateTime\":\"" + calStartDateTime + "\"";
    // requestJson += ",\"timeZone\":\"" + timeZone + "\"}";
    //
    // requestJson += ",\"end\":";
    // requestJson += "{\"dateTime\":\"" + calEndDateTime + "\"";
    // requestJson += ",\"timeZone\":\"" + timeZone + "\"}";
    // requestJson += "}";
    //
    // ResponseEntity<String> responseJson = null;
    // if (outlookCalendarDto.getCaldSycId().length() > 0) {
    // RestTemplate restTemplate = new RestTemplate();
    // HttpClient httpClient = HttpClientBuilder.create().build();
    // restTemplate.setRequestFactory(new
    // HttpComponentsClientHttpRequestFactory(httpClient));
    // HttpHeaders reqHeaders = new HttpHeaders();
    // reqHeaders.setContentType(MediaType.APPLICATION_JSON);
    // reqHeaders.set("Authorization", "Bearer " + accessToken);
    // HttpEntity<String> requestEntity = new HttpEntity<String>(requestJson,
    // reqHeaders);
    // responseJson = restTemplate.exchange(requestUrl, HttpMethod.PATCH,
    // requestEntity, String.class);
    // } else {
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // headers.set("Authorization", "Bearer " + accessToken);
    // HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
    // responseJson = apiCall.exchange(requestUrl, HttpMethod.POST, entity,
    // String.class);
    // }
    // JSONObject jsonObject = new JSONObject(responseJson.getBody());
    // resBody.put("caldSycId", jsonObject.getString("id"));
    // }
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw new RuntimeException();
    // }
    // return resBody;
    // }

    @Override
    public List<Integer> downloadOutlookCalendarToLocalData(Integer cusId, List<Integer> calIdList, String accessToken,
            String currentDate) throws ParseException {
        String nextLink = null;
        int top = 100;
        int skip = 0;
        do {
            Customers customers = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            RestTemplate apiCall = new RestTemplate();
            String requestUrl = "";
            if (nextLink == null) {
                requestUrl = calendarApiUri
                        + "events?$select=transactionId,subject,bodyPreview,start,end,isAllDay&$count=true&$top=" + top;
            } else {
                requestUrl = calendarApiUri
                        + "events?$select=transactionId,subject,bodyPreview,start,end,isAllDay&$count=true&$top=" + top
                        + "&$skip=" + skip;
            }
            requestUrl += "&$filter=Start/DateTime ge '" + currentDate + "T00:00:00Z'";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
            JSONObject response = new JSONObject(responseJson.getBody());

            if (response.has("@odata.nextLink")) {
                nextLink = response.getString("@odata.nextLink");
                skip = skip + top;
            } else {
                nextLink = null;
            }

            JSONArray jsonArray = response.getJSONArray("value");
            // System.out.println("Outlook Response : " + jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject res = new JSONObject(jsonArray.get(i).toString());
                OutlookCalendarDto outlookCalendarDto = new OutlookCalendarDto();
                outlookCalendarDto.setCaldSycId(res.getString("id"));
                outlookCalendarDto.setCalTitle(res.getString("subject"));
                outlookCalendarDto.setCalDescription(res.getString("bodyPreview"));
                if (res.has("location")) {
                    outlookCalendarDto.setLocation(res.getString("location"));
                } else {
                    // Get the description (may contain HTML)
                    String rawDescription = res.optString("bodyPreview", "");

                    // Optional: strip HTML tags to get plain text
                    String plainDescription = rawDescription.replaceAll("<[^>]*>", "");

                    // Call your existing location service
                    LocationResultDto result = this.locationService.extractLocations(plainDescription);
                    if ("Location Found".equals(result.getStatus())) {
                        // Use the first detected location, or join multiple
                        String locationsAsString = String.join(", ", result.getExtractedLocations());
                        outlookCalendarDto.setLocation(locationsAsString);
                    } else {
                        outlookCalendarDto.setLocation(null);
                    }
                }
                outlookCalendarDto.setCalAllDay("false");
                if (res.has("isAllDay")) {
                    if (res.getBoolean("isAllDay")) {
                        outlookCalendarDto.setCalAllDay("true");
                    }
                }

                try {
                    if (res.has("bodyPreview")) {
                        List<String> calAttendees = new ArrayList<>();
                        String regexUrl = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$";
                        Matcher m = Pattern.compile(regexUrl).matcher(res.getString("bodyPreview"));
                        while (m.find()) {
                            if (!calAttendees.contains(m.group())) {
                                calAttendees.add(m.group().toLowerCase());
                            }
                        }
                        outlookCalendarDto.setCalAttendees(calAttendees);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }

                if (res.has("start")) {
                    JSONObject start = res.getJSONObject("start");
                    // outlookCalendarDto.setCalStartDateTime(this.commonService.convertTimeZoneToDbDate(start.getString("dateTime")));
                    outlookCalendarDto.setCalTimeZone(start.getString("timeZone"));

                    outlookCalendarDto.setCalStartDateTime(this.commonService.convertEventTimeZoneToUserDB(
                            this.commonService.convertTimeZoneToDbDate(start.getString("dateTime")).substring(0, 19),
                            start.getString("timeZone"), serverDatabaseTimeZone));
                }
                if (res.has("end")) {
                    JSONObject end = res.getJSONObject("end");
                    // outlookCalendarDto.setCalEndDateTime(this.commonService.convertTimeZoneToDbDate(end.getString("dateTime")));

                    outlookCalendarDto.setCalEndDateTime(this.commonService.convertEventTimeZoneToUserDB(
                            this.commonService.convertTimeZoneToDbDate(end.getString("dateTime")).substring(0, 19),
                            end.getString("timeZone"), serverDatabaseTimeZone));
                }

                CalendarDetails calendarDetails = null;
                Integer caldId = 0;
                try {
                    calendarDetails = calendarDetailsRepository.findSycId(outlookCalendarDto.getCaldSycId(), "outlook");
                    caldId = calendarDetails != null ? calendarDetails.getId() : null;
                    if (caldId == null) {
                        caldId = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    caldId = 0;
                    throw new RuntimeException();
                }
                if (caldId == 0) {
                    Calendar findCalendar = null;
                    Integer calId = 0;
                    try {
                        findCalendar = calendarRepository.findData(cusId, outlookCalendarDto.getCalTitle(),
                                outlookCalendarDto.getCalStartDateTime(), outlookCalendarDto.getCalEndDateTime());
                        calId = findCalendar != null ? findCalendar.getId() : null;
                        if (calId == null) {
                            calId = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        calId = 0;
                        throw new RuntimeException();
                    }

                    Calendar calendar = new Calendar();
                    if (outlookCalendarDto.getCalTitle() == null) {
                        calendar.setCalTitle("");
                    } else {
                        calendar.setCalTitle(outlookCalendarDto.getCalTitle());
                    }
                    calendar.setCalDescription(outlookCalendarDto.getCalDescription());
                    calendar.setCustomers(customers);
                    // if (calId > 0) {
                    // calendar.setSubcusId(findCalendar.getSubcusId());
                    // }
                    calendar.setCalAllDay(outlookCalendarDto.getCalAllDay());

                    try {
                        if (!outlookCalendarDto.getCalAttendees().isEmpty()) {
                            String attnd = "{\"attendees\":[";
                            int t = 0;
                            for (String attendees : outlookCalendarDto.getCalAttendees()) {
                                if (t != 0) {
                                    attnd += ",";
                                }
                                attnd += "\"" + attendees + "\"";
                                t++;
                            }
                            attnd += "]}";
                            calendar.setCalAttendees(attnd);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }

                    try {
                        if (outlookCalendarDto.getCalAllDay().equals("false")) {
                            calendar.setCalStartDateTime(
                                    this.commonService.convertDate(outlookCalendarDto.getCalStartDateTime()));
                            calendar.setCalEndDateTime(
                                    this.commonService.convertDate(outlookCalendarDto.getCalEndDateTime()));
                            calendar.setCalTimeZone(outlookCalendarDto.getCalTimeZone());
                        } else {
                            calendar.setCalStartDateTime(
                                    this.commonService.convertDateOnly(outlookCalendarDto.getCalStartDateTime()));
                            calendar.setCalEndDateTime(
                                    this.commonService.convertDateOnly(outlookCalendarDto.getCalEndDateTime()));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }

                    if (calId == 0) {
                        calendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                        calendar.setCalNotification("Y");
                        calendar.setCalType("outlook");
                    } else {
                        calendar.setCalCreatedDateTime(findCalendar.getCalCreatedDateTime());
                        calendar.setId(calId);
                        calendar.setCalNotification(findCalendar.getCalNotification());
                        calendar.setCalType(findCalendar.getCalType());
                    }
                    calendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                    calendar.setCalEventReminder("event");
                    calendar.setCalReminderSubject("");
                    calendar.setCalReminderType("");
                    calendar.setCalMyPageId(0L);
                    calendar.setCalSmsSstId(0L);
                    calendar.setCalScheduleDateTime(new Timestamp(System.currentTimeMillis()));
                    calId = this.calendarRepository.save(calendar).getId();
                    calIdList.add(calId);

                    CalendarDetails cd = new CalendarDetails();
                    cd.setCaldType("outlook");
                    cd.setCalendar(calendar);
                    cd.setCaldSycId(outlookCalendarDto.getCaldSycId());
                    calendarDetailsRepository.save(cd);
                } else {
                    try {
                        Calendar calendar = calendarRepository.findCalendarById(calendarDetails.getCalendar().getId(),
                                cusId);
                        if (calendar != null) {
                            if (outlookCalendarDto.getCalTitle() == null) {
                                calendar.setCalTitle("");
                            } else {
                                calendar.setCalTitle(outlookCalendarDto.getCalTitle());
                            }
                            calendar.setCalDescription(outlookCalendarDto.getCalDescription());
                            calendar.setCalAllDay(outlookCalendarDto.getCalAllDay());

                            try {
                                if (outlookCalendarDto.getCalAllDay().equals("false")) {
                                    calendar.setCalStartDateTime(
                                            this.commonService.convertDate(outlookCalendarDto.getCalStartDateTime()));
                                    calendar.setCalEndDateTime(
                                            this.commonService.convertDate(outlookCalendarDto.getCalEndDateTime()));
                                    // calendar.setCalTimeZone(outlookCalendarDto.getCalTimeZone());
                                } else {
                                    calendar.setCalStartDateTime(this.commonService
                                            .convertDateOnly(outlookCalendarDto.getCalStartDateTime()));
                                    calendar.setCalEndDateTime(
                                            this.commonService.convertDateOnly(outlookCalendarDto.getCalEndDateTime()));
                                }
                            } catch (ParseException ee) {
                                ee.printStackTrace();
                                throw new RuntimeException();
                            }
                            calendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                            calendar.setCalEventReminder("event");
                            calendar.setCalReminderSubject("");
                            calendar.setCalReminderType("");
                            calendar.setCalMyPageId(0L);
                            calendar.setCalSmsSstId(0L);
                            calendar.setCalScheduleDateTime(new Timestamp(System.currentTimeMillis()));
                            Integer calId = calendarRepository.save(calendar).getId();
                            calIdList.add(calId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }

                }
            }
        } while (nextLink != null);
        return calIdList;
    }

    @Override
    public Map<String, Object> revoke(Integer cusId) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            Customers customers = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            String accessToken = customers.getOutlookCalendarAccessToken();
            String refreshToken = customers.getOutlookCalendarRefreshToken();

            if (accessToken == null) {
                accessToken = "";
            }
            if (refreshToken == null) {
                refreshToken = "";
            }

            if (!accessToken.equals("")) {
                // Clear local Outlook Calendar connection state first so local disconnection succeeds
                customers.setOutlookCalendarAccessToken("");
                customers.setOutlookCalendarRefreshToken("");
                customers.setOutlookCalendarEmail(null);
                this.customersRepository.save(customers);

                // Attempt to call Microsoft's revokeSignInSessions API.
                // We wrap this in a nested try-catch block so that if the external call fails
                // (e.g., token already expired/invalid, tenant permissions, or network issue),
                // it does not crash or block the local disconnection.
                try {
                    // Try to refresh the token to ensure we have a valid one for revocation, if a refresh token is present.
                    String tokenToUse = accessToken;
                    if (!refreshToken.equals("")) {
                        try {
                            // Call MS Graph token refresh endpoint directly to get a fresh token for revocation,
                            // without persisting the new token back to the now-disconnected customer.
                            String requestUrl = calendarAuthorizeApiUri + "token";
                            RestTemplate apiCall = new RestTemplate();
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            HttpEntity<String> entity = new HttpEntity<String>(
                                    "client_id=" + clientId + "&redirect_uri=" + redirectURI
                                            + "&client_secret=" + clientSecret + "&scope=" + scope
                                            + "&refresh_token=" + refreshToken + "&grant_type=refresh_token",
                                    headers);
                            String message = apiCall.postForObject(requestUrl, entity, String.class);
                            JSONObject jsonObject = new JSONObject(message);
                            tokenToUse = jsonObject.getString("access_token");
                        } catch (Exception rfe) {
                            // Ignore refresh error and try with existing accessToken
                        }
                    }

                    if (tokenToUse != null && !tokenToUse.equals("")) {
                        RestTemplate apiCall = new RestTemplate();
                        String requestUrl = "https://graph.microsoft.com/v1.0/me/revokeSignInSessions";
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Authorization", "Bearer " + tokenToUse);
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<String> entity = new HttpEntity<String>("", headers);
                        apiCall.postForObject(requestUrl, entity, String.class);
                    }
                } catch (Exception ex) {
                    boolean shouldSuppress = false;
                    if (ex instanceof org.springframework.web.client.HttpStatusCodeException) {
                        org.springframework.web.client.HttpStatusCodeException hsce = 
                            (org.springframework.web.client.HttpStatusCodeException) ex;
                        String body = hsce.getResponseBodyAsString();
                        int statusCode = hsce.getStatusCode().value();
                        if (statusCode == 403 || statusCode == 401 || (body != null && (body.contains("Authorization_RequestDenied") || body.contains("invalid_token")))) {
                            shouldSuppress = true;
                        }
                    }
                    if (!shouldSuppress) {
                        System.err.println("Warning: Microsoft token revocation failed: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", "error");
        }
        return resBody;
    }

    @Override
    public void uploadLocalDataToOutlookCalendar(Integer cusId, String timeZone, List<Integer> calIdList,
            String currentDate) {
        List<Calendar> calendarList = null;
        if (calIdList.isEmpty()) {
            calendarList = calendarRepository.findListNotCalendarDataAll(cusId, currentDate);
        } else {
            calendarList = calendarRepository.findListNotCalendarData(cusId, calIdList, currentDate);
        }

        if (!calendarList.isEmpty()) {
            for (Calendar cdar : calendarList) {
                int flagCalendarDetails = 0;
                CalendarDetails calendarDetails = null;
                Integer caldId = 0;
                try {
                    calendarDetails = calendarDetailsRepository.findByCalId(cdar.getId(), "outlook");
                    caldId = calendarDetails != null ? calendarDetails.getId() : null;
                    if (caldId == null) {
                        caldId = 0;
                    } else {
                        if (calendarDetails.getCaldSycId().length() > 0) {
                            flagCalendarDetails = 1;
                            calendarDetailsRepository.deleteById(caldId);
                            try {
                                int count = calendarDetailsRepository.findCountRecords(cdar.getId());
                                if (count == 0) {
                                    calendarRepository.deleteByCalId(cdar.getId(), cusId);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (flagCalendarDetails == 0) {
                    OutlookCalendarDto outlookCalendarDto = new OutlookCalendarDto();
                    outlookCalendarDto.setCalTitle(cdar.getCalTitle());
                    outlookCalendarDto.setCalDescription(cdar.getCalDescription());

                    try {
                        List<String> calAttendees = new ArrayList<>();
                        String calAtt = cdar.getCalAttendees();
                        if (calAtt != null) {
                            JSONObject data = new JSONObject(calAtt);
                            JSONArray attendees = data.getJSONArray("attendees");
                            if (attendees != null && !attendees.isEmpty()) {
                                for (int i = 0; i < attendees.length(); i++) {
                                    calAttendees.add(attendees.get(i).toString());
                                }
                                if (!calAttendees.isEmpty()) {
                                    outlookCalendarDto.setCalAttendees(calAttendees);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (cdar.getCalTimeZone() != null) {
                        outlookCalendarDto.setCalTimeZone(cdar.getCalTimeZone());
                    } else {
                        outlookCalendarDto.setCalTimeZone(timeZone);
                    }

                    try {
                        if (Objects.nonNull(cdar.getCalStartDateTime())) {
                            outlookCalendarDto.setCalStartDateTime(
                                    this.commonService.dateObjectToDisplayDate(cdar.getCalStartDateTime()));

                            outlookCalendarDto.setCalStartDateTime(this.commonService.convertEventTimeZoneToUser(
                                    outlookCalendarDto.getCalStartDateTime(), serverDatabaseTimeZone,
                                    this.commonService.changeTimeZoneName(outlookCalendarDto.getCalTimeZone())));
                        }
                        if (Objects.nonNull(cdar.getCalEndDateTime())) {
                            outlookCalendarDto.setCalEndDateTime(
                                    this.commonService.dateObjectToDisplayDate(cdar.getCalEndDateTime()));

                            outlookCalendarDto.setCalEndDateTime(this.commonService.convertEventTimeZoneToUser(
                                    outlookCalendarDto.getCalEndDateTime(), serverDatabaseTimeZone,
                                    this.commonService.changeTimeZoneName(outlookCalendarDto.getCalTimeZone())));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    outlookCalendarDto.setCalAllDay(cdar.getCalAllDay());

                    CalendarDetails cd = new CalendarDetails();
                    try {
                        Map<String, Object> innerResBody = new HashMap<>();
                        outlookCalendarDto.setCaldSycId("");
                        innerResBody = this.saveEvent(cusId, outlookCalendarDto);
                        if (innerResBody.get("error").equals("")) {
                            cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cd.setCalendar(cdar);
                    cd.setCaldType("outlook");
                    cd.setId(caldId);
                    this.calendarDetailsRepository.save(cd);
                }
            }
        }
    }

    @Override
    public void deleteEvent(Integer cusId, String eventId) {
        try {
            Customers customers = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            String accessToken = customers.getOutlookCalendarAccessToken();
            if (accessToken == null) {
                accessToken = "";
            }
            if (!accessToken.equals("")) {
                accessToken = refreshToken(customers.getOutlookCalendarRefreshToken(), customers);
                if (accessToken != null) {
                    RestTemplate apiCall = new RestTemplate();
                    String requestUrl = calendarApiUri + "events/" + eventId;
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + accessToken);
                    HttpEntity<String> entity = new HttpEntity<String>("", headers);
                    ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.DELETE, entity,
                            String.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getOutlookLastUpdatedTime(String accessToken) {
        RestTemplate apiCall = new RestTemplate();
        String requestUrl = calendarApiUri + "events?$select=transactionId,subject,bodyPreview,start,end,isAllDay";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
        return new JSONObject(responseJson.getBody());
    }

    private JSONObject createDateTimeObject(String dtStr, String tz) {
        String formatted = dtStr.replace(" ", "T");
        // Usually logic here to ensure it is yyyy-MM-ddTHH:mm:ss
        JSONObject dtObj = new JSONObject();
        dtObj.put("dateTime", formatted);
        dtObj.put("timeZone", tz);
        return dtObj;
    }
}
