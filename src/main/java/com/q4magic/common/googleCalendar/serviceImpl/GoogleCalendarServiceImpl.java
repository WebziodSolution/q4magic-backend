package com.q4magic.common.googleCalendar.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.q4magic.common.dto.LocationResultDto;
import com.q4magic.common.dto.googleCalendar.*;
import com.q4magic.common.googleCalendar.service.GoogleCalendarService;
import com.q4magic.common.locationExtractorService.LocationExtractorService;
import com.q4magic.common.models.CalendarDetails;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.CalendarDetailsRepository;
import com.q4magic.common.repository.CalendarRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.*;
import java.util.*;

@Service(value = "GoogleCalendarService")
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CalendarDetailsRepository calendarDetailsRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private LocationExtractorService locationService;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${google.calendar.clientId}")
    private String clientId;

    @Value("${google.calendar.clientSecret}")
    private String clientSecret;

    @Value("${google.calendar.redirectUri}")
    private String redirectUri;

    @Value("${google.calendar.calendarApiUri}")
    private String calendarApiUri;

    @Value("${server.database.timezone}")
    String serverDatabaseTimeZone;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    @Override
    public GoogleAuthUrlResponse getAuthorizationUrl(Integer customerId) {
        try {
            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

            String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + encodedRedirectUri +
                    "&response_type=code" +
                    "&include_granted_scopes=true" +
                    "&scope=" + URLEncoder.encode(CALENDAR_SCOPE, StandardCharsets.UTF_8) +
                    "&access_type=offline" +
                    "&prompt=consent" +
                    "&state=" + customerId;

            return new GoogleAuthUrlResponse(url);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate authorization URL", e);
        }
    }

    @Override
    public void handleOAuthCallback(String code, String state) {
        Customers customer = customersRepository.findById(Integer.parseInt(state))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + state));

        try {
            RestTemplate apiCall = new RestTemplate();
            String requestUrl = "https://oauth2.googleapis.com/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("code", code);
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("redirect_uri", redirectUri);
            form.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            String message = apiCall.postForObject(requestUrl, entity, String.class);
            JSONObject jsonObject = new JSONObject(message);

            if (jsonObject.has("access_token")) {
                customer.setGoogleCalendarAccessToken(jsonObject.getString("access_token"));
            }

            if (jsonObject.has("refresh_token")) {
                customer.setGoogleCalendarRefreshToken(jsonObject.getString("refresh_token"));
            }

            customersRepository.save(customer);
            this.getEmail(Integer.parseInt(state));
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            boolean alreadyConnected = customer.getGoogleCalendarAccessToken() != null &&
                    !customer.getGoogleCalendarAccessToken().isEmpty();

            if (e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    body != null &&
                    body.contains("invalid_grant") &&
                    alreadyConnected) {
                return;
            }
            throw e;
        } catch (Exception e) {
            System.err.println("Error in handleOAuthCallback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OAuth callback failed", e);
        }
    }

    @Override
    public void getEmail(Integer userId) {
        try {
            Customers customer = customersRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + userId));

            String accessToken = customer.getGoogleCalendarAccessToken();
            if (accessToken == null) {
                accessToken = "";
            }
            if (!accessToken.equals("")) {
                accessToken = refreshToken(customer.getGoogleCalendarRefreshToken());
                if (accessToken != null) {
                    RestTemplate apiCall = new RestTemplate();
                    String requestUrl = "https://www.googleapis.com/calendar/v3/calendars/primary";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + accessToken);
                    HttpEntity<String> entity = new HttpEntity<String>("", headers);
                    ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity,
                            String.class);
                    JSONObject response = new JSONObject(responseJson.getBody());

                    String googleCalendarEmail = response.getString("id");
                    customer.setGoogleCalendarAccessToken(accessToken);
                    customer.setGoogleCalendarEmail(googleCalendarEmail);
                    this.customersRepository.save(customer);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to getEmail", e);
        }
    }

    // @Override
    // public String refreshToken(String refreshToken) {
    // if (refreshToken == null || refreshToken.isBlank()) {
    // throw new RuntimeException("Google refresh token missing. User must reconnect
    // Google Calendar.");
    // }
    //
    // try {
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    //
    // MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    // form.add("client_id", clientId);
    // form.add("client_secret", clientSecret);
    // form.add("refresh_token", refreshToken);
    // form.add("grant_type", "refresh_token");
    // // NOTE: redirect_uri is NOT required for refresh_token grant; remove it.
    //
    // HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form,
    // headers);
    //
    // // Use the newer endpoint you already declared as TOKEN_URL
    // String message = restTemplate.postForObject(TOKEN_URL, entity, String.class);
    //
    // JSONObject jsonObject = new JSONObject(message);
    // return jsonObject.getString("access_token");
    //
    // } catch (HttpClientErrorException e) {
    // String body = e.getResponseBodyAsString();
    //
    // // IMPORTANT: invalid_grant means refresh token is dead -> user must re-auth
    // if (e.getStatusCode() == HttpStatus.BAD_REQUEST
    // && body != null
    // && body.contains("invalid_grant")) {
    //
    // throw new RuntimeException("Google refresh token is expired/revoked. User
    // must reconnect Google Calendar.", e);
    // }
    //
    // throw new RuntimeException("Failed to refresh Google access token: " + body,
    // e);
    // } catch (Exception e) {
    // throw new RuntimeException("Failed to refresh Google access token.", e);
    // }
    // }
    @Override
    public String refreshToken(String refreshToken) {
        try {
            RestTemplate apiCall = new RestTemplate();
            String requestUrl = "https://www.googleapis.com/oauth2/v4/token";
            Map<String, String> params = new HashMap<String, String>();
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("refresh_token", refreshToken);
            params.put("grant_type", "refresh_token");
            params.put("redirect_uri", redirectUri);
            String message = apiCall.postForObject(requestUrl, params, String.class);
            // log.error("RefreshToken : "+ message);
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("RefreshGoogleCalendarToken : " + e);
        }
    }

    @Override
    public Map<String, Object> getUserTimezone(Integer cusId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Customers customer = customersRepository.findById(cusId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + cusId));
            String accessToken = customer.getGoogleCalendarAccessToken();
            if (accessToken == null) {
                accessToken = "";
            }
            if (!accessToken.equals("")) {
                accessToken = refreshToken(customer.getGoogleCalendarRefreshToken());
                if (accessToken != null) {
                    customer.setGoogleCalendarAccessToken(accessToken);
                    this.customersRepository.save(customer);
                    String timeZone = getUserCalendarTimezone(accessToken);
                    resBody.put("calTimeZone", timeZone);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resBody;
    }

    @Override
    public List<GoogleCalendarEventDto> listEvents(Integer customerId, Instant timeMin, Instant timeMax) {
        try {
            Customers customer = customersRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + customerId));

            if (customer.getGoogleCalendarAccessToken() == null &&
                    customer.getGoogleCalendarRefreshToken() == null) {
                throw new IllegalStateException("Customer not connected to Google Calendar");
            }

            String accessToken = ensureValidAccessToken(customer);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String url = UriComponentsBuilder.fromHttpUrl(EVENTS_URL + "events?maxResults=100&singleEvents=true")
                    .queryParam("timeMin", timeMin.toString())
                    .queryParam("timeMax", timeMax.toString())
                    .queryParam("singleEvents", "true")
                    .queryParam("orderBy", "startTime")
                    .build()
                    .toUriString();

            System.out.println("Calling Google Calendar events URL: " + url);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            } catch (HttpClientErrorException.Unauthorized ex) {
                // Access token expired – refresh and retry once
                refreshAccessToken(customer);
                headers.setBearerAuth(customer.getGoogleCalendarAccessToken());
                requestEntity = new HttpEntity<>(headers);
                response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            }

            String body = response.getBody();
            MediaType ct = response.getHeaders().getContentType();
            System.out.println("Google events content-type: " + ct);
            // If Google ever returns HTML, log it so you can see what’s going on.
            if (ct != null && ct.includes(MediaType.TEXT_HTML)) {
                System.err.println("Google returned HTML instead of JSON:");
                System.err.println(body);
                throw new RuntimeException("Unexpected HTML response from Google Calendar API");
            }

            // Parse JSON manually into your DTO
            ObjectMapper mapper = new ObjectMapper();
            GoogleEventsResponse eventsResponse = mapper.readValue(body, GoogleEventsResponse.class);

            List<GoogleCalendarEventDto> result = new ArrayList<>();

            if (eventsResponse != null && eventsResponse.getItems() != null) {
                for (GoogleEventItem item : eventsResponse.getItems()) {
                    ZonedDateTime start = parseEventDateTime(item.getStart());
                    ZonedDateTime end = parseEventDateTime(item.getEnd());

                    result.add(new GoogleCalendarEventDto(
                            item.getId(),
                            item.getSummary(),
                            item.getDescription(),
                            item.getLocation(),
                            start,
                            end));
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to list Google Calendar events", e);
        }
    }

    @Override
    public String getUserCalendarTimezone(String accessToken) {
        try {
            RestTemplate apiCall = new RestTemplate();
            String requestUrl = "https://www.googleapis.com/calendar/v3/users/me/settings/timezone";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
            JSONObject response = new JSONObject(responseJson.getBody());
            // log.info("GetUserCalendarTimezone : "+ response.getString("value"));
            return response.getString("value");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> saveEvent(Integer customerId, GoogleCalendarDto googleCalendarDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            Customers customer = customersRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + customerId));

            String accessToken = customer.getGoogleCalendarAccessToken();
            if (accessToken == null)
                accessToken = "";

            if (!accessToken.isEmpty()) {
                accessToken = refreshToken(customer.getGoogleCalendarRefreshToken());
                if (accessToken != null) {
                    customer.setGoogleCalendarAccessToken(accessToken);
                    customersRepository.save(customer);

                    String timeZone = getUserCalendarTimezone(accessToken);
                    if (googleCalendarDto.getCalTimeZone() != null && !googleCalendarDto.getCalTimeZone().isBlank()) {
                        timeZone = googleCalendarDto.getCalTimeZone();
                    }

                    // ✅ URL
                    String baseUrl = calendarApiUri + "events"; // should be .../calendars/primary/events
                    boolean isUpdate = googleCalendarDto.getCaldSycId() != null
                            && !googleCalendarDto.getCaldSycId().isBlank();

                    String requestUrl = isUpdate
                            ? (baseUrl + "/" + googleCalendarDto.getCaldSycId())
                            : baseUrl;

                    // ✅ sendUpdates must be query param (not in JSON)
                    UriComponentsBuilder ucb = UriComponentsBuilder.fromHttpUrl(requestUrl);
                    if (googleCalendarDto.getCalAttendees() != null && !googleCalendarDto.getCalAttendees().isEmpty()) {
                        ucb.queryParam("sendUpdates", "all");
                    }
                    requestUrl = ucb.toUriString();

                    // ✅ Build body using Map (safe JSON)
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("summary", googleCalendarDto.getCalTitle() == null ? "" : googleCalendarDto.getCalTitle());

                    String desc = googleCalendarDto.getCalDescription();
                    if (desc != null) {
                        desc = desc.replaceAll("\\r\\n|\\r|\\n", "<br>");
                    }
                    body.put("description", desc == null ? "" : desc);

                    // ✅ attendees
                    if (googleCalendarDto.getCalAttendees() != null && !googleCalendarDto.getCalAttendees().isEmpty()) {
                        List<Map<String, String>> attendees = new ArrayList<>();
                        for (String email : googleCalendarDto.getCalAttendees()) {
                            if (email == null || email.isBlank())
                                continue;
                            Map<String, String> a = new HashMap<>();
                            a.put("email", email.trim());
                            // displayName is optional; keep if you want
                            a.put("displayName", email.trim());
                            attendees.add(a);
                        }
                        if (!attendees.isEmpty())
                            body.put("attendees", attendees);
                    }

                    // ✅ start/end must be OBJECT, not array
                    boolean allDay = "true".equalsIgnoreCase(String.valueOf(googleCalendarDto.getCalAllDay()));

                    if (!allDay) {
                        // IMPORTANT: convertDateTimeToTimeZone must return RFC3339 (e.g.
                        // 2026-01-05T10:00:00+05:30)
                        Map<String, Object> start = new LinkedHashMap<>();
                        start.put("dateTime",
                                commonService.convertDateTimeToTimeZone(googleCalendarDto.getCalStartDateTime()));
                        start.put("timeZone", timeZone);

                        Map<String, Object> end = new LinkedHashMap<>();
                        end.put("dateTime",
                                commonService.convertDateTimeToTimeZone(googleCalendarDto.getCalEndDateTime()));
                        end.put("timeZone", timeZone);

                        body.put("start", start);
                        body.put("end", end);
                    } else {
                        // all-day uses "date" (yyyy-mm-dd)
                        if (googleCalendarDto.getCalStartDateTime() != null) {
                            Map<String, Object> start = new LinkedHashMap<>();
                            start.put("date", commonService.dbDate(googleCalendarDto.getCalStartDateTime()));
                            body.put("start", start);
                        }
                        if (googleCalendarDto.getCalEndDateTime() != null) {
                            Map<String, Object> end = new LinkedHashMap<>();
                            end.put("date", commonService.dbDate(googleCalendarDto.getCalEndDateTime()));
                            body.put("end", end);
                        }
                    }

                    String requestJson = new ObjectMapper().writeValueAsString(body);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(accessToken);

                    HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

                    RestTemplate apiCall = new RestTemplate();
                    ResponseEntity<String> responseJson = apiCall.exchange(requestUrl,
                            isUpdate ? HttpMethod.PUT : HttpMethod.POST, entity, String.class);

                    JSONObject jsonObject = new JSONObject(responseJson.getBody());
                    resBody.put("caldSycId", jsonObject.getString("id"));
                }
            }

            return resBody;

        } catch (HttpClientErrorException e) {
            // ✅ This will show the REAL reason from Google (very important)
            throw new RuntimeException("Google Calendar API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEvent(Integer customerId, String eventId) {
        try {
            Customers customer = customersRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + customerId));
            String accessToken = customer.getGoogleCalendarAccessToken();
            if (accessToken == null) {
                accessToken = "";
            }
            if (!accessToken.equals("")) {
                accessToken = refreshToken(customer.getGoogleCalendarRefreshToken());
                if (accessToken != null) {
                    customer.setGoogleCalendarAccessToken(accessToken);
                    this.customersRepository.save(customer);

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONObject getGoogleLastUpdatedTime(String accessToken) {
        RestTemplate apiCall = new RestTemplate();
        String requestUrl = calendarApiUri + "events?maxResults=1&singleEvents=true";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
        return new JSONObject(responseJson.getBody());
    }

    @Override
    public List<Integer> downloadGoogleCalendarToLocalData(Integer cusId, List<Integer> calIdList, String accessToken,
            String currentDate) {
        String pageToken = null;
        do {
            RestTemplate apiCall = new RestTemplate();
            String requestUrl = calendarApiUri + "events?maxResults=100&singleEvents=true";
            if (pageToken != null) {
                requestUrl += "&pageToken=" + pageToken;
            }
            requestUrl += "&timeMin=" + currentDate + "T00:00:00Z";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseJson = apiCall.exchange(requestUrl, HttpMethod.GET, entity, String.class);
            JSONObject response = new JSONObject(responseJson.getBody());

            String userCalendarTimeZone = "";
            if (response.has("timeZone")) {
                userCalendarTimeZone = response.getString("timeZone");
            }

            JSONArray jsonArray = response.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject res = new JSONObject(jsonArray.get(i).toString());
                if (res.has("summary")) {
                    GoogleCalendarDto googleCalendarDto = new GoogleCalendarDto();
                    googleCalendarDto.setCaldSycId(res.getString("id"));
                    googleCalendarDto.setCalTitle(res.getString("summary"));
                    if (res.has("description")) {
                        googleCalendarDto.setCalDescription(res.getString("description"));
                    }
                    if (res.has("location")) {
                        googleCalendarDto.setLocation(res.getString("location"));
                    } else {
                        // Get the description (may contain HTML)
                        String rawDescription = res.optString("description", "");

                        // Optional: strip HTML tags to get plain text
                        String plainDescription = rawDescription.replaceAll("<[^>]*>", "");

                        // Call your existing location service
//                        LocationResultDto result = this.locationService.extractLocations(plainDescription);
//                        if ("Location Found".equals(result.getStatus())) {
//                            // Use the first detected location, or join multiple
//                            String locationsAsString = String.join(", ", result.getExtractedLocations());
//                            googleCalendarDto.setLocation(locationsAsString);
//                        } else {
//                            googleCalendarDto.setLocation(null);
//                        }
                        googleCalendarDto.setLocation(null);
                    }

                    try {
                        if (res.has("attendees")) {
                            List<String> calAttendees = new ArrayList<>();
                            JSONArray attendees = res.getJSONArray("attendees");
                            for (int j = 0; j < attendees.length(); j++) {
                                JSONObject obj = attendees.getJSONObject(j);
                                if (!calAttendees.contains(obj.getString("email"))) {
                                    calAttendees.add(obj.getString("email"));
                                }
                            }
                            googleCalendarDto.setCalAttendees(calAttendees);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }

                    try {
                        if (res.has("start")) {
                            JSONObject start = res.getJSONObject("start");
                            if (start.has("dateTime")) {
                                // googleCalendarDto.setCalStartDateTime(this.commonService.convertTimeZoneToDbDate(start.getString("dateTime")));
                                googleCalendarDto
                                        .setCalStartDateTime(this.commonService.convertEventTimeZoneToUserDB(
                                                this.commonService.convertTimeZoneToDbDate(start.getString("dateTime"))
                                                        .substring(0, 19),
                                                userCalendarTimeZone, serverDatabaseTimeZone));

                                googleCalendarDto.setCalAllDay("false");
                                if (start.has("timeZone")) {
                                    googleCalendarDto.setCalTimeZone(start.getString("timeZone"));
                                }
                            }
                            if (start.has("date")) {
                                googleCalendarDto.setCalStartDateTime(start.getString("date"));
                                googleCalendarDto.setCalAllDay("true");
                            }
                        }

                        if (res.has("end")) {
                            JSONObject end = res.getJSONObject("end");
                            if (end.has("dateTime")) {
                                // googleCalendarDto.setCalEndDateTime(this.commonService.convertTimeZoneToDbDate(end.getString("dateTime")));
                                googleCalendarDto
                                        .setCalEndDateTime(this.commonService.convertEventTimeZoneToUserDB(
                                                this.commonService.convertTimeZoneToDbDate(end.getString("dateTime"))
                                                        .substring(0, 19),
                                                userCalendarTimeZone, serverDatabaseTimeZone));

                                googleCalendarDto.setCalAllDay("false");
                                if (end.has("timeZone")) {
                                    googleCalendarDto.setCalTimeZone(end.getString("timeZone"));
                                }
                            }
                            if (end.has("date")) {
                                googleCalendarDto.setCalEndDateTime(end.getString("date"));
                                // googleCalendarDto.setCalEndDateTime(this.commonService.dateObjectToDbDate(this.commonService.minusDays(this.commonService.convertDateOnly(end.getString("date")),-1)));
                                googleCalendarDto.setCalAllDay("true");
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }

                    CalendarDetails calendarDetails = null;
                    Integer caldId = 0;
                    try {
                        calendarDetails = this.calendarDetailsRepository.findSycId(googleCalendarDto.getCaldSycId(),
                                "google");
                        if (calendarDetails != null) {
                            caldId = calendarDetails.getId();
                        }
                    } catch (Exception e) {
                        caldId = 0;
                        e.printStackTrace();
                    }

                    if (caldId == 0) {
                        Calendar findCalendar = null;
                        Integer calId = 0;
                        try {
                            findCalendar = this.calendarRepository.findData(cusId, googleCalendarDto.getCalTitle(),
                                    googleCalendarDto.getCalStartDateTime(), googleCalendarDto.getCalEndDateTime());
                            if (findCalendar != null) {
                                calId = findCalendar.getId();
                            }
                        } catch (Exception e) {
                            calId = 0;
                            e.printStackTrace();
                        }
                        Calendar calendar = new Calendar();
                        calendar.setCalTitle(googleCalendarDto.getCalTitle());
                        calendar.setCalDescription(googleCalendarDto.getCalDescription());
                        calendar.setLocation(googleCalendarDto.getLocation());
                        Customers customers = this.customersRepository.findById(cusId)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                        calendar.setCustomers(customers);

                        calendar.setCalAllDay(googleCalendarDto.getCalAllDay());

                        try {
                            if (googleCalendarDto.getCalAttendees() != null) {
                                String attnd = "{\"attendees\":[";
                                int t = 0;
                                for (String attendees : googleCalendarDto.getCalAttendees()) {
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
                        }

                        try {
                            if (googleCalendarDto.getCalAllDay().equals("false")) {
                                calendar.setCalStartDateTime(
                                        this.commonService.convertDate(googleCalendarDto.getCalStartDateTime()));
                                calendar.setCalEndDateTime(
                                        this.commonService.convertDate(googleCalendarDto.getCalEndDateTime()));
                                calendar.setCalTimeZone(googleCalendarDto.getCalTimeZone());
                            } else {
                                calendar.setCalStartDateTime(
                                        this.commonService.convertDateOnly(googleCalendarDto.getCalStartDateTime()));
                                calendar.setCalEndDateTime(
                                        this.commonService.convertDateOnly(googleCalendarDto.getCalEndDateTime()));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (calId == 0) {
                            calendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                            calendar.setCalNotification("Y");
                            calendar.setCalType("google");
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

                        calId = calendarRepository.save(calendar).getId();
                        calIdList.add(calId);

                        CalendarDetails cd = new CalendarDetails();
                        cd.setCalendar(calendarRepository.getReferenceById(calId)); // ✅ must set relation
                        cd.setCaldType("google");
                        cd.setCaldSycId(googleCalendarDto.getCaldSycId());
                        calendarDetailsRepository.save(cd); // ✅ id remains null (INSERT)

                    } else {
                        try {
                            Calendar calendar = calendarRepository
                                    .findCalendarById(calendarDetails.getCalendar().getId(), cusId);
                            if (calendar != null) {
                                if (googleCalendarDto.getCalTitle() != null) {
                                    calendar.setCalTitle(googleCalendarDto.getCalTitle());
                                    calendar.setCalDescription(googleCalendarDto.getCalDescription());
                                    calendar.setLocation(googleCalendarDto.getLocation());
                                    calendar.setCalAllDay(googleCalendarDto.getCalAllDay());

                                    try {
                                        if (googleCalendarDto.getCalAllDay().equals("false")) {
                                            calendar.setCalStartDateTime(this.commonService
                                                    .convertDate(googleCalendarDto.getCalStartDateTime()));
                                            calendar.setCalEndDateTime(this.commonService
                                                    .convertDate(googleCalendarDto.getCalEndDateTime()));
                                            // calendar.setCalTimeZone(googleCalendarDto.getCalTimeZone());
                                        } else {
                                            calendar.setCalStartDateTime(this.commonService
                                                    .convertDateOnly(googleCalendarDto.getCalStartDateTime()));
                                            calendar.setCalEndDateTime(this.commonService
                                                    .convertDateOnly(googleCalendarDto.getCalEndDateTime()));
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
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
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (response.has("nextPageToken")) {
                pageToken = response.getString("nextPageToken");
            } else {
                pageToken = null;
            }
        } while (pageToken != null);
        return calIdList;
    }

    @Override
    public void uploadLocalDataToGoogleCalendar(Integer cusId, String timeZone, List<Integer> calIdList,
            String currentDate) throws ParseException {
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
                    calendarDetails = calendarDetailsRepository.findByCalId(cdar.getId(), "google");
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
                    GoogleCalendarDto googleCalendarDto = new GoogleCalendarDto();
                    googleCalendarDto.setCalTitle(cdar.getCalTitle());
                    googleCalendarDto.setCalDescription(cdar.getCalDescription());

                    List<String> calAttendees = new ArrayList<>();
                    try {
                        String calAtt = cdar.getCalAttendees();
                        if (calAtt != null && calAtt != "") {
                            JSONObject data = new JSONObject(calAtt);
                            JSONArray attendees = data.getJSONArray("attendees");
                            for (int i = 0; i < attendees.length(); i++) {
                                calAttendees.add(attendees.get(i).toString());
                            }
                            if (!calAttendees.isEmpty()) {
                                googleCalendarDto.setCalAttendees(calAttendees);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (cdar.getCalTimeZone() != null) {
                        googleCalendarDto.setCalTimeZone(cdar.getCalTimeZone());
                    } else {
                        googleCalendarDto.setCalTimeZone(timeZone);
                    }
                    try {
                        if (Objects.nonNull(cdar.getCalStartDateTime())) {
                            googleCalendarDto.setCalStartDateTime(
                                    this.commonService.dateObjectToDisplayDate(cdar.getCalStartDateTime()));

                            googleCalendarDto.setCalStartDateTime(this.commonService.convertEventTimeZoneToUser(
                                    googleCalendarDto.getCalStartDateTime(), serverDatabaseTimeZone,
                                    this.commonService.changeTimeZoneName(googleCalendarDto.getCalTimeZone())));
                        }
                        if (Objects.nonNull(cdar.getCalEndDateTime())) {
                            googleCalendarDto.setCalEndDateTime(
                                    this.commonService.dateObjectToDisplayDate(cdar.getCalEndDateTime()));

                            googleCalendarDto.setCalEndDateTime(this.commonService.convertEventTimeZoneToUser(
                                    googleCalendarDto.getCalEndDateTime(), serverDatabaseTimeZone,
                                    this.commonService.changeTimeZoneName(googleCalendarDto.getCalTimeZone())));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    googleCalendarDto.setCalAllDay(cdar.getCalAllDay());

                    CalendarDetails cd = new CalendarDetails();
                    try {
                        Map<String, Object> innerResBody = new HashMap<>();
                        googleCalendarDto.setCaldSycId("");
                        innerResBody = saveEvent(cusId, googleCalendarDto);
                        if (innerResBody.get("error").equals("")) {
                            cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cd.setCalendar(cdar);
                    cd.setCaldType("google");
                    calendarDetailsRepository.save(cd);
                }
            }
        }
    }

    private String ensureValidAccessToken(Customers customer) {
        if (customer.getGoogleCalendarAccessToken() == null &&
                customer.getGoogleCalendarRefreshToken() != null) {
            refreshAccessToken(customer);
        }
        return customer.getGoogleCalendarAccessToken();
    }

    private void refreshAccessToken(Customers customer) {
        if (customer.getGoogleCalendarRefreshToken() == null) {
            throw new IllegalStateException("No refresh token stored for customer " + customer.getId());
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", customer.getGoogleCalendarRefreshToken());
        form.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        ResponseEntity<GoogleOAuthTokenResponse> response = restTemplate.postForEntity(TOKEN_URL, requestEntity,
                GoogleOAuthTokenResponse.class);

        GoogleOAuthTokenResponse body = response.getBody();
        if (body == null || body.getAccessToken() == null) {
            throw new RuntimeException("Failed to refresh access token from Google");
        }

        customer.setGoogleCalendarAccessToken(body.getAccessToken());
        customersRepository.save(customer);
    }

    private ZonedDateTime parseEventDateTime(GoogleEventDateTime src) {
        if (src == null)
            return null;

        if (src.getDateTime() != null) {
            // full datetime with timezone
            return ZonedDateTime.parse(src.getDateTime());
        }

        if (src.getDate() != null) {
            // all-day event (no time)
            LocalDate date = LocalDate.parse(src.getDate());
            return date.atStartOfDay(ZoneId.systemDefault());
        }

        return null;
    }

    @Override
    public Map<String, Object> revoke(Integer userId) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            Customers customers = this.customersRepository.findById(userId).orElseThrow(
                    () -> new RuntimeException("Customer not found with ID: " + userId));
            String accessToken = customers.getGoogleCalendarAccessToken();
            String refreshToken = customers.getGoogleCalendarRefreshToken();

            if (accessToken == null) {
                accessToken = "";
            }
            if (refreshToken == null) {
                refreshToken = "";
            }

            if (!accessToken.equals("")) {
                // Clear local Google Calendar connection state first so local disconnection succeeds
                customers.setGoogleCalendarAccessToken("");
                customers.setGoogleCalendarRefreshToken("");
                customers.setGoogleCalendarEmail(null);
                customers.setGoogleCalendarSyncTime(null);
                this.customersRepository.save(customers);

                // Attempt to call Google's revoke API to invalidate the tokens on Google's end.
                // We wrap this in a nested try-catch block so that if the external call fails
                // (e.g., token already expired/invalid or network issue), it does not crash or block
                // the local disconnection.
                try {
                    // Revoking the refresh token will invalidate it and all access tokens generated from it.
                    String tokenToRevoke = !refreshToken.equals("") ? refreshToken : accessToken;
                    RestTemplate apiCall = new RestTemplate();
                    String requestUrl = "https://oauth2.googleapis.com/revoke?token=" + tokenToRevoke;
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    HttpEntity<String> entity = new HttpEntity<String>("", headers);
                    apiCall.postForObject(requestUrl, entity, String.class);
                } catch (Exception ex) {
                    boolean isInvalidToken = false;
                    if (ex instanceof org.springframework.web.client.HttpStatusCodeException) {
                        String body = ((org.springframework.web.client.HttpStatusCodeException) ex).getResponseBodyAsString();
                        if (body != null && body.contains("invalid_token")) {
                            isInvalidToken = true;
                        }
                    }
                    if (!isInvalidToken) {
                        System.err.println("Warning: Google token revocation failed: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", "Failed to revoke access token");
        }
        return resBody;
    }
}
