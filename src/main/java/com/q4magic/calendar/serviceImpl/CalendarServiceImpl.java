package com.q4magic.calendar.serviceImpl;

import com.q4magic.calendar.service.CalendarService;
import com.q4magic.common.dto.DeleteEventDto;
import com.q4magic.common.dto.LocationResultDto;
import com.q4magic.common.dto.MeetingsDto;
import com.q4magic.common.dto.outlookCalendar.OutlookCalendarDto;
import com.q4magic.common.locationExtractorService.LocationExtractorService;
import com.q4magic.common.models.*;
import com.q4magic.common.dto.CalendarDto;
import com.q4magic.common.dto.googleCalendar.GoogleCalendarDto;
import com.q4magic.common.googleCalendar.service.GoogleCalendarService;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.outlookCalendar.service.OutlookCalendarService;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.meetings.service.MeetingsService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service(value = "CalendarService")
public class CalendarServiceImpl implements CalendarService {

    @Value("${server.database.timezone}")
    String serverDatabaseTimeZone;

    @Value("${siteUrl}")
    String siteUrl;

    @Value("${companyName}")
    String companyName;

    @Value("${siteUrlWWW}")
    String siteUrlWWW;

    @Value("${siteUrlWWWDisplay}")
    String siteUrlWWWDisplay;

    @Value("${mainCompanyName}")
    String mainCompanyName;

    @Value("${siteUrlAddress}")
    String siteUrlAddress;

    @Value("${siteUrlAddressBr}")
    String siteUrlAddressBr;

    @Value("${companyNumber}")
    String companyNumber;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CalendarDetailsRepository calendarDetailsRepository;

    @Autowired
    private OutlookCalendarService outlookCalendarService;

    @Autowired
    private MeetingsService meetingsService;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Autowired
    private LocationExtractorService locationService;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Override
    public Map<String, Object> getEventList(Integer cusId, String calStartDateTime, String calEndDateTime, String cusTimeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String timeZone = serverDatabaseTimeZone;
            Date startDate = null;
            Date endDate = null;

            try {
                if (Objects.nonNull(calStartDateTime) && !calStartDateTime.isBlank()) {
                    String startDb = this.commonService.dbDate(calStartDateTime);     // yyyy-MM-dd
                    startDate = this.commonService.convertDateOnly(startDb);          // Date
                }

                if (Objects.nonNull(calEndDateTime) && !calEndDateTime.isBlank()) {
                    String endDb = this.commonService.dbDate(calEndDateTime);         // yyyy-MM-dd
                    endDate = this.commonService.convertDateOnly(endDb);              // Date
                    endDate = this.commonService.addOneDays(endDate, 24);             // +24 hours (next day)
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            List<Calendar> list = this.calendarRepository.findCalendarList(cusId, startDate, endDate);
            List<CalendarDto> calendarDtoList = new ArrayList<>();

            for (Calendar calendar : list) {
                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setId(calendar.getId());
                calendarDto.setTitle(calendar.getCalTitle());
                calendarDto.setDescription(calendar.getCalDescription());
                calendarDto.setLocation(calendar.getLocation());
                calendarDto.setAllDay(Boolean.parseBoolean(calendar.getCalAllDay()));
                calendarDto.setCustomerId(calendar.getCustomers().getId());

                String displayTimeZone = cusTimeZone;
                if (calendar.getCalType() != null
                        && calendar.getCalType().equals(companyName.toLowerCase().replace(" ", ""))) {
                    displayTimeZone = calendar.getCalTimeZone();
                }
                calendarDto.setCalTimeZone(displayTimeZone);

                calendarDto.setCalAttendees(calendar.getCalAttendees());
                List<String> contactList = new ArrayList<>();
                if (calendar.getCalNumbers() != null && !calendar.getCalNumbers().isEmpty()) {
                    contactList = Arrays.asList(calendar.getCalNumbers().split(","));
                }
                calendarDto.setContactList(contactList);
                calendarDto.setCalAetId(calendar.getCalAetId());
                try {
//                    if (Objects.nonNull(calendar.getCalStartDateTime())) {
//                        calendarDto.setStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, cusTimeZone));
//                    }
//                    if (Objects.nonNull(calendar.getCalEndDateTime())) {
//                        calendarDto.setEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, cusTimeZone));
//                    }
//                    if (Objects.nonNull(calendar.getCalStartDateTime())) {
//                        calendarDto.setDisplayStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, displayTimeZone));
//                    }
//                    if (Objects.nonNull(calendar.getCalEndDateTime())) {
//                        calendarDto.setDisplayEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, displayTimeZone));
//                    }
                    if (calendar.getCalStartDateTime() != null) {
                        // Convert database datetime to required string format (MM/dd/yyyy HH:mm:ss)
                        String formattedDateTime = commonService.displayDateTime(calendar.getCalStartDateTime().toString());

                        // Convert to member's time zone
                        String startStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, cusTimeZone);
                        // Convert to display time zone
                        String displayStartStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);

                        // allDay handling: replace time part with "00:00:00"
                        if (calendar.getCalAllDay().equals("true")) {   // or dto.getAllDay() depending on your DTO
                            if (startStr != null && startStr.contains(" ")) {
                                startStr = startStr.split(" ")[0] + " 00:00:00";
                            }
                            if (displayStartStr != null && displayStartStr.contains(" ")) {
                                displayStartStr = displayStartStr.split(" ")[0] + " 00:00:00";
                            }
                        }

                        calendarDto.setStart(startStr);
                        calendarDto.setDisplayStart(displayStartStr);
                    }

                    if (calendar.getCalEndDateTime() != null) {
                        String formattedDateTime = commonService.displayDateTime(calendar.getCalEndDateTime().toString());

                        String endStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, cusTimeZone);
                        String displayEndStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);

                        if (calendar.getCalAllDay().equals("true")) {
                            if (endStr != null && endStr.contains(" ")) {
                                endStr = endStr.split(" ")[0] + " 00:00:00";
                            }
                            if (displayEndStr != null && displayEndStr.contains(" ")) {
                                displayEndStr = displayEndStr.split(" ")[0] + " 00:00:00";
                            }
                        }

                        calendarDto.setEnd(endStr);
                        calendarDto.setDisplayEnd(displayEndStr);
                    }
                    if (Objects.nonNull(calendar.getCalScheduleDateTime())) {
                        calendarDto.setCalScheduleDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalScheduleDateTime().toString()), timeZone, displayTimeZone));
                    }
                    if (Objects.nonNull(calendar.getCalRepeatEndDate())) {
                        calendarDto.setCalRepeatEndDate(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalRepeatEndDate().toString()), timeZone, displayTimeZone));
                    }
                    if (Objects.nonNull(calendar.getCalRepeatDate())) {
                        calendarDto.setCalRepeatDate(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalRepeatDate().toString()), timeZone, displayTimeZone));
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                calendarDto.setCalType(calendar.getCalType());
                calendarDto.setCalEventReminder(calendar.getCalEventReminder());
                calendarDto.setCalReminderSubject(calendar.getCalReminderSubject());
                calendarDto.setCalReminderType(calendar.getCalReminderType());
                calendarDto.setCalMyPageId(calendar.getCalMyPageId());
                calendarDto.setCalSmsSstId(calendar.getCalSmsSstId());

                calendarDto.setCalParentId(calendar.getCalParentId());
                calendarDto.setCalRepeatEvery(calendar.getCalRepeatEvery());
                calendarDto.setCalRepeatType(calendar.getCalRepeatType());
                calendarDto.setCalRepeatEveryType(calendar.getCalRepeatEveryType());
                calendarDto.setCalRepeatDayName(calendar.getCalRepeatDayName());
                calendarDto.setCalRepeatSelectedOption(calendar.getCalRepeatSelectedOption());

                calendarDtoList.add(calendarDto);
            }
            resBody.put("eventList", calendarDtoList);
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getEvent(Integer cusId, Integer calId, String cusTimeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String timeZone = serverDatabaseTimeZone;

            Calendar calendar = calendarRepository.findCalendarById(calId, cusId);
            CalendarDto calendarDto = new CalendarDto();
            calendarDto.setId(calendar.getId());
            calendarDto.setTitle(calendar.getCalTitle());
            calendarDto.setDescription(calendar.getCalDescription());
            calendarDto.setLocation(calendar.getLocation());
            calendarDto.setAllDay(Boolean.parseBoolean(calendar.getCalAllDay()));
            calendarDto.setCustomerId(calendar.getCustomers().getId());

            String displayTimeZone = cusTimeZone;
            if (calendar.getCalType() != null && calendar.getCalType().equals(companyName.toLowerCase().replace(" ", ""))) {
                displayTimeZone = calendar.getCalTimeZone();
            }
            calendarDto.setCalTimeZone(displayTimeZone);
            calendarDto.setCalAttendees(calendar.getCalAttendees());
            List<String> contactList = new ArrayList<>();
            if (calendar.getCalNumbers() != null && !calendar.getCalNumbers().isEmpty()) {
                contactList = Arrays.asList(calendar.getCalNumbers().split(","));
            }
            calendarDto.setContactList(contactList);
            calendarDto.setCalAetId(calendar.getCalAetId());
            try {
//                if (Objects.nonNull(calendar.getCalStartDateTime())) {
//                    calendarDto.setStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, displayTimeZone));
//                }
//                if (Objects.nonNull(calendar.getCalEndDateTime())) {
//                    calendarDto.setEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, displayTimeZone));
//                }
//                if (Objects.nonNull(calendar.getCalStartDateTime())) {
//                    calendarDto.setDisplayStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, displayTimeZone));
//                }
//                if (Objects.nonNull(calendar.getCalEndDateTime())) {
//                    calendarDto.setDisplayEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, displayTimeZone));
//                }

                if (calendar.getCalStartDateTime() != null) {
                    // Convert database datetime to required string format (MM/dd/yyyy HH:mm:ss)
                    String formattedDateTime = commonService.displayDateTime(calendar.getCalStartDateTime().toString());

                    // Convert to member's time zone
                    String startStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);
                    // Convert to display time zone
                    String displayStartStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);

                    // allDay handling: replace time part with "00:00:00"
                    if (calendar.getCalAllDay().equals("true")) {   // or dto.getAllDay() depending on your DTO
                        if (startStr != null && startStr.contains(" ")) {
                            startStr = startStr.split(" ")[0] + " 00:00:00";
                        }
                        if (displayStartStr != null && displayStartStr.contains(" ")) {
                            displayStartStr = displayStartStr.split(" ")[0] + " 00:00:00";
                        }
                    }

                    calendarDto.setStart(startStr);
                    calendarDto.setDisplayStart(displayStartStr);
                }

                if (calendar.getCalEndDateTime() != null) {
                    String formattedDateTime = commonService.displayDateTime(calendar.getCalEndDateTime().toString());

                    String endStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);
                    String displayEndStr = commonService.convertEventTimeZoneToUser(formattedDateTime, timeZone, displayTimeZone);

                    if (calendar.getCalAllDay().equals("true")) {
                        if (endStr != null && endStr.contains(" ")) {
                            endStr = endStr.split(" ")[0] + " 00:00:00";
                        }
                        if (displayEndStr != null && displayEndStr.contains(" ")) {
                            displayEndStr = displayEndStr.split(" ")[0] + " 00:00:00";
                        }
                    }

                    calendarDto.setEnd(endStr);
                    calendarDto.setDisplayEnd(displayEndStr);
                }
                if (Objects.nonNull(calendar.getCalScheduleDateTime())) {
                    calendarDto.setCalScheduleDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalScheduleDateTime().toString()), timeZone, displayTimeZone));
                }
                if (Objects.nonNull(calendar.getCalRepeatEndDate())) {
                    calendarDto.setCalRepeatEndDate(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalRepeatEndDate().toString()), timeZone, displayTimeZone));
                }
                if (Objects.nonNull(calendar.getCalRepeatDate())) {
                    calendarDto.setCalRepeatDate(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalRepeatDate().toString()), timeZone, displayTimeZone));
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            calendarDto.setCalType(calendar.getCalType());
            calendarDto.setCalEventReminder(calendar.getCalEventReminder());
            calendarDto.setCalReminderSubject(calendar.getCalReminderSubject());
            calendarDto.setCalReminderType(calendar.getCalReminderType());
            calendarDto.setCalMyPageId(calendar.getCalMyPageId());
            calendarDto.setCalSmsSstId(calendar.getCalSmsSstId());

            calendarDto.setCalParentId(calendar.getCalParentId());
            calendarDto.setCalRepeatEvery(calendar.getCalRepeatEvery());
            calendarDto.setCalRepeatType(calendar.getCalRepeatType());
            calendarDto.setCalRepeatEveryType(calendar.getCalRepeatEveryType());
            calendarDto.setCalRepeatDayName(calendar.getCalRepeatDayName());
            calendarDto.setCalRepeatSelectedOption(calendar.getCalRepeatSelectedOption());

            Meetings meetings = this.meetingsRepository.findByCalendarId(calId);
            if (meetings != null) {
                calendarDto.setMeetingId(meetings.getId());
                calendarDto.setContactIds(meetings.getContactIds());
            }

            resBody.put("event", calendarDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resBody;
    }

    @Override
    public Map<String, Object> getSync(Integer cusId, String timeZone) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String currentDate = "";
            Customers customer = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + cusId));
            currentDate = this.commonService.dbToDdTimeZone(this.commonService.dateObjectToDbDateTime(new Date()), timeZone).substring(0, 10);

            Boolean googleCalendar = false;
            Boolean outlookCalendar = false;

            if (Objects.nonNull(customer.getGoogleCalendarAccessToken())) {
                if (!customer.getGoogleCalendarAccessToken().trim().isEmpty()) {
                    googleCalendar = true;
                }
            }
            if (Objects.nonNull(customer.getOutlookCalendarAccessToken())) {
                if (!customer.getOutlookCalendarAccessToken().trim().isEmpty()) {
                    outlookCalendar = true;
                }
            }

            String googleAccessToken = customer.getGoogleCalendarAccessToken();
            String outlookAccessToken = customer.getOutlookCalendarAccessToken();
            if (googleAccessToken == null) {
                googleAccessToken = "";
            }
            if (outlookAccessToken == null) {
                outlookAccessToken = "";
            }

            int googleFlag = 0;
            String googleTimeZone = "";
            List<Integer> googleCalIdList = new ArrayList<>();
            int outlookFlag = 0;
            String outlookTimeZone = "";
            List<Integer> outlookCalIdList = new ArrayList<>();

            if (customer.getDefaultCalendar() != null) {
                if (customer.getDefaultCalendar().equals("google")) {
                    if (googleCalendar == true) {
                        if (!googleAccessToken.equals("")) {
                            googleAccessToken = this.googleCalendarService.refreshToken(customer.getGoogleCalendarRefreshToken());
                            if (googleAccessToken != null) {
                                customer.setGoogleCalendarAccessToken(googleAccessToken);
                                this.customersRepository.save(customer);
                                googleTimeZone = this.googleCalendarService.getUserCalendarTimezone(googleAccessToken);

                                // Check sync time start
                                if (customer.getGoogleCalendarSyncTime() != null) {
                                    JSONObject response = this.googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                                    if (response.getString("updated").equals(customer.getGoogleCalendarSyncTime())) {
                                        googleFlag = 1;
                                    }
                                }
                                // Check sync time end

                                if (googleFlag == 0) {
                                    googleCalIdList = this.googleCalendarService.downloadGoogleCalendarToLocalData(cusId, googleCalIdList, googleAccessToken, currentDate);
                                }
                            }
                        }
                    }
                    if (outlookCalendar == true) {
                        if (!outlookAccessToken.equals("")) {
                            outlookAccessToken = this.outlookCalendarService.refreshToken(customer.getOutlookCalendarRefreshToken(), customer);
                            if (outlookAccessToken != null) {
                                outlookTimeZone = this.outlookCalendarService.getUserCalendarTimezone(outlookAccessToken);
                                if (outlookFlag == 0) {
                                    outlookCalIdList = this.outlookCalendarService.downloadOutlookCalendarToLocalData(cusId, outlookCalIdList, outlookAccessToken, currentDate);
                                }
                            }
                        }
                    }
                    if (googleCalendar == true) {
                        if (googleFlag == 0) {
                            this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                        } else {
                            googleCalIdList = calendarRepository.findTypeCalendarList(cusId, "google");
                            this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                        }
                        // Save sync time start
                        JSONObject response = googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                        customer.setGoogleCalendarSyncTime(response.getString("updated"));
                        this.customersRepository.save(customer);
                        // Save sync time end
                    }
                    if (outlookCalendar == true) {
                        if (outlookFlag == 0) {
                            this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                        } else {
                            outlookCalIdList = calendarRepository.findTypeCalendarList(cusId, "outlook");
                            this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                        }
                    }
                } else {
                    if (outlookCalendar == true) {
                        if (!outlookAccessToken.equals("")) {
                            outlookAccessToken = this.outlookCalendarService.refreshToken(customer.getOutlookCalendarRefreshToken(), customer);
                            if (outlookAccessToken != null) {
                                outlookTimeZone = this.outlookCalendarService.getUserCalendarTimezone(outlookAccessToken);
                                if (outlookFlag == 0) {
                                    outlookCalIdList = this.outlookCalendarService.downloadOutlookCalendarToLocalData(cusId, outlookCalIdList, outlookAccessToken, currentDate);
                                }
                            }
                        }
                    }
                    if (googleCalendar == true) {
                        if (!googleAccessToken.equals("")) {
                            googleAccessToken = this.googleCalendarService.refreshToken(customer.getGoogleCalendarRefreshToken());
                            if (googleAccessToken != null) {
                                customer.setGoogleCalendarAccessToken(googleAccessToken);
                                this.customersRepository.save(customer);
                                googleTimeZone = this.googleCalendarService.getUserCalendarTimezone(googleAccessToken);

                                // Check sync time start
                                if (customer.getGoogleCalendarSyncTime() != null) {
                                    JSONObject response = this.googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                                    if (response.getString("updated").equals(customer.getGoogleCalendarSyncTime())) {
                                        googleFlag = 1;
                                    }
                                }
                                // Check sync time end

                                if (googleFlag == 0) {
                                    googleCalIdList = this.googleCalendarService.downloadGoogleCalendarToLocalData(cusId, googleCalIdList, googleAccessToken, currentDate);
                                }
                            }
                        }
                    }
                    if (outlookCalendar == true) {
                        if (outlookFlag == 0) {
                            this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                        } else {
                            outlookCalIdList = calendarRepository.findTypeCalendarList(cusId, "outlook");
                            this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                        }
                    }
                    if (googleCalendar == true) {
                        if (googleFlag == 0) {
                            this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                        } else {
                            googleCalIdList = calendarRepository.findTypeCalendarList(cusId, "google");
                            this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                        }
                        // Save sync time start
                        JSONObject response = googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                        customer.setGoogleCalendarSyncTime(response.getString("updated"));
                        this.customersRepository.save(customer);
                        // Save sync time end
                    }
                }
            } else {
                // Download google calendar to local data start
                if (googleCalendar == true) {
                    if (!googleAccessToken.equals("")) {
                        googleAccessToken = this.googleCalendarService.refreshToken(customer.getGoogleCalendarRefreshToken());
                        if (googleAccessToken != null) {
                            customer.setGoogleCalendarAccessToken(googleAccessToken);
                            this.customersRepository.save(customer);
                            googleTimeZone = this.googleCalendarService.getUserCalendarTimezone(googleAccessToken);

                            // Check sync time start
                            if (customer.getGoogleCalendarSyncTime() != null) {
                                JSONObject response = this.googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                                if (response.getString("updated").equals(customer.getGoogleCalendarSyncTime())) {
                                    googleFlag = 1;
                                }
                            }
                            // Check sync time end

                            if (googleFlag == 0) {
                                googleCalIdList = this.googleCalendarService.downloadGoogleCalendarToLocalData(cusId, googleCalIdList, googleAccessToken, currentDate);
                            }
                        }
                    }
                }

                if (outlookCalendar == true) {
                    if (!outlookAccessToken.equals("")) {
                        outlookAccessToken = this.outlookCalendarService.refreshToken(customer.getOutlookCalendarRefreshToken(), customer);
                        if (outlookAccessToken != null) {
                            outlookTimeZone = this.outlookCalendarService.getUserCalendarTimezone(outlookAccessToken);
                            if (outlookFlag == 0) {
                                outlookCalIdList = this.outlookCalendarService.downloadOutlookCalendarToLocalData(cusId, outlookCalIdList, outlookAccessToken, currentDate);
                            }
                        }
                    }
                }
                // Upload local data to google calendar start
                if (googleCalendar == true) {
                    if (googleFlag == 0) {
                        this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                    } else {
                        googleCalIdList = calendarRepository.findTypeCalendarList(cusId, "google");
                        this.googleCalendarService.uploadLocalDataToGoogleCalendar(cusId, googleTimeZone, googleCalIdList, currentDate);
                    }
                    // Save sync time start
                    JSONObject response = googleCalendarService.getGoogleLastUpdatedTime(googleAccessToken);
                    customer.setGoogleCalendarSyncTime(response.getString("updated"));
                    this.customersRepository.save(customer);
                    // Save sync time end
                }
                // Upload local data to google calendar end

                // Upload local data to outlook calendar start
                if (outlookCalendar == true) {
                    if (outlookFlag == 0) {
                        this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                    } else {
                        outlookCalIdList = calendarRepository.findTypeCalendarList(cusId, "outlook");
                        this.outlookCalendarService.uploadLocalDataToOutlookCalendar(cusId, outlookTimeZone, outlookCalIdList, currentDate);
                    }
                }
                // Upload local data to outlook calendar end
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resBody;
    }

    @Override
    public Map<String, Object> saveEvent(Integer cusId, CalendarDto calendarDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            Customers customer = this.customersRepository.findById(cusId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + cusId));

            Map<String, Object> innerResBody = new HashMap<>();
            innerResBody = this.googleCalendarService.getUserTimezone(cusId);
            String timeZone = null;
            if (innerResBody.get("calTimeZone") != null) {
                timeZone = innerResBody.get("calTimeZone").toString();
            }

            if (calendarDto.getCalTimeZone() != null) {
                if (timeZone != null) {
                    calendarDto.setCalTimeZone(timeZone);
                }
            }

            List<String> cladTypeList = new ArrayList<>();
            List<String> cladTypeListNew = new ArrayList<>();

            if (Objects.nonNull(customer.getGoogleCalendarAccessToken())) {
                if (!customer.getGoogleCalendarAccessToken().trim().isEmpty()) {
                    cladTypeList.add("google");
                    cladTypeListNew.add("google");
                }
            }
            if (Objects.nonNull(customer.getOutlookCalendarAccessToken())) {
                if (!customer.getOutlookCalendarAccessToken().trim().isEmpty()) {
                    cladTypeList.add("outlook");
                    cladTypeListNew.add("outlook");
                }
            }

            List<String> calAttendees = new ArrayList<>();
            String calAtt = calendarDto.getCalAttendees();
            if (calAtt != null && !calAtt.isEmpty()) {
                calAtt = calAtt.trim();
                if (calAtt.startsWith("[")) {
                    // It's a JSON array -> parse directly
                    JSONArray attendeesArray = new JSONArray(calAtt);
                    for (int i = 0; i < attendeesArray.length(); i++) {
                        calAttendees.add(attendeesArray.get(i).toString());
                    }
                } else if (calAtt.startsWith("{")) {
                    // It's a JSON object with an "attendees" field (original expected format)
                    JSONObject data = new JSONObject(calAtt);
                    JSONArray attendees = data.getJSONArray("attendees");
                    for (int i = 0; i < attendees.length(); i++) {
                        calAttendees.add(attendees.get(i).toString());
                    }
                } else {
                    // Fallback: treat as plain comma-separated? Or log warning
                    System.err.println("Unexpected calAttendees format: " + calAtt);
                }
            }

            GoogleCalendarDto googleCalendarDto = null;
            if (cladTypeList.contains("google")) {
                googleCalendarDto = new GoogleCalendarDto();
                googleCalendarDto.setCalTitle(calendarDto.getTitle());
                googleCalendarDto.setCalDescription(calendarDto.getDescription());
                googleCalendarDto.setCalStartDateTime(calendarDto.getStart());
                googleCalendarDto.setCalEndDateTime(calendarDto.getEnd());
                googleCalendarDto.setCalAllDay(calendarDto.getAllDay().toString());
                googleCalendarDto.setCalTimeZone(calendarDto.getCalTimeZone());
                googleCalendarDto.setCalAttendees(calAttendees);
            }

            OutlookCalendarDto outlookCalendarDto = null;
            if (cladTypeList.contains("outlook")) {
                outlookCalendarDto = new OutlookCalendarDto();
                outlookCalendarDto.setCalTitle(calendarDto.getTitle());
                outlookCalendarDto.setCalDescription(calendarDto.getDescription());
                outlookCalendarDto.setCalStartDateTime(calendarDto.getStart());
                outlookCalendarDto.setCalEndDateTime(calendarDto.getEnd());
                outlookCalendarDto.setCalAllDay(calendarDto.getAllDay().toString());
                outlookCalendarDto.setCalTimeZone(calendarDto.getCalTimeZone());
                outlookCalendarDto.setCalAttendees(calAttendees);
            }

            try {
                if (Objects.nonNull(calendarDto.getStart())) {
                    calendarDto.setStart(this.commonService.dbDateTime(calendarDto.getStart()));
                }
                if (Objects.nonNull(calendarDto.getEnd())) {
                    calendarDto.setEnd(this.commonService.dbDateTime(calendarDto.getEnd()));
                }
                if (Objects.nonNull(calendarDto.getCalRepeatEndDate())) {
                    calendarDto.setCalRepeatEndDate(this.commonService.dbDateTime(calendarDto.getCalRepeatEndDate()));
                }
                if (Objects.nonNull(calendarDto.getCalRepeatDate())) {
                    calendarDto.setCalRepeatDate(this.commonService.dbDateTime(calendarDto.getCalRepeatDate()));
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            // Set Database Timezone : Not Delete
            timeZone = serverDatabaseTimeZone;

            Calendar calendar = new Calendar();
            calendar.setId(calendarDto.getId());
            calendar.setCalTitle(calendarDto.getTitle());
            calendar.setCalDescription(calendarDto.getDescription());
            calendar.setCustomers(customer);
            calendar.setCalAllDay(calendarDto.getAllDay().toString());
            calendar.setCalTimeZone(calendarDto.getCalTimeZone());
            calendar.setCalAttendees(calendarDto.getCalAttendees());
            String numbers = "";
            if (calendarDto.getContactList() != null) {
                for (String number : calendarDto.getContactList()) {
                    if (numbers.isEmpty()) {
                        numbers = number;
                    } else {
                        numbers += "," + number;
                    }
                }
            }
            calendar.setCalNumbers(numbers);
            calendar.setCalType(companyName.toLowerCase().replace(" ", ""));

            calendar.setCalEventReminder("event");
            calendar.setCalReminderSubject("");
            calendar.setCalReminderType("");
            calendar.setCalMyPageId(0L);
            calendar.setCalSmsSstId(0L);
            calendar.setCalScheduleDateTime(new Timestamp(System.currentTimeMillis()));

            calendar.setCalParentId(calendarDto.getCalParentId());
            calendar.setCalRepeatEvery(calendarDto.getCalRepeatEvery());
            calendar.setCalRepeatType(calendarDto.getCalRepeatType());
            calendar.setCalRepeatEveryType(calendarDto.getCalRepeatEveryType());
            calendar.setCalRepeatDayName(calendarDto.getCalRepeatDayName());
            calendar.setCalRepeatSelectedOption(calendarDto.getCalRepeatSelectedOption());

            try {
                if (Objects.nonNull(calendarDto.getStart())) {
                    calendar.setCalStartDateTime(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getStart(), calendarDto.getCalTimeZone(), timeZone)));
                }
                if (Objects.nonNull(calendarDto.getEnd())) {
                    calendar.setCalEndDateTime(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getEnd(), calendarDto.getCalTimeZone(), timeZone)));
                }
                if (Objects.nonNull(calendarDto.getCalRepeatEndDate())) {
                    calendar.setCalRepeatEndDate(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getCalRepeatEndDate(), calendarDto.getCalTimeZone(), timeZone)));
                }
                if (Objects.nonNull(calendarDto.getCalRepeatDate())) {
                    calendar.setCalRepeatDate(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getCalRepeatDate(), calendarDto.getCalTimeZone(), timeZone)));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            if (calendarDto.getId() == null) {
                calendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                calendar.setCalNotification("N");
            } else {
                Calendar tmpCalendar = this.calendarRepository.findCalendarById(calendarDto.getId(), cusId);
                calendar.setCalCreatedDateTime(tmpCalendar.getCalCreatedDateTime());
            }

            calendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));

            // Get the description (may contain HTML)
            String rawDescription = calendarDto.getDescription();

            // Optional: strip HTML tags to get plain text
            String plainDescription = rawDescription.replaceAll("<[^>]*>", "");

            // Call your existing location service
            LocationResultDto result = this.locationService.extractLocations(plainDescription);
            if ("Location Found".equals(result.getStatus())) {
                // Use the first detected location, or join multiple
                String locationsAsString = String.join(", ", result.getExtractedLocations());
                calendar.setLocation(locationsAsString);
            } else {
                calendar.setLocation(null);
            }

//            if (customer.getSubUserType() != null && customer.getSubUserType().getName().toLowerCase().equals("sales representative")) {
//
//
//            }
            Integer oldMeetingCount = 0;
            Integer newMeetingCount = 0;
            Set<String> newAttendeeSet = calAttendees.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            Set<String> oldAttendeeSet = new HashSet<>();

            List<Calendar> oldCalendersList = this.calendarRepository.findCalendarByCustomerId(cusId);
            Performance performance = this.performanceRepository.findByCustomerId(cusId) != null ? this.performanceRepository.findByCustomerId(cusId) : new Performance();

            for (Calendar oldCalendar : oldCalendersList) {

                String calAttendeesJson = oldCalendar.getCalAttendees();
                if (calAttendeesJson != null && !calAttendeesJson.isEmpty()) {
                    calAttendeesJson = calAttendeesJson.trim();
                    if (calAttendeesJson.startsWith("[")) {
                        // It's a JSON array -> parse directly
                        JSONArray attendeesArray = new JSONArray(calAttendeesJson);
                        for (int i = 0; i < attendeesArray.length(); i++) {
                            oldAttendeeSet.add(attendeesArray.get(i).toString());
                        }
                    } else if (calAttendeesJson.startsWith("{")) {
                        // It's a JSON object with an "attendees" field (original expected format)
                        JSONObject data = new JSONObject(calAttendeesJson);
                        JSONArray attendees = data.getJSONArray("attendees");
                        for (int i = 0; i < attendees.length(); i++) {
                            oldAttendeeSet.add(attendees.get(i).toString());
                        }
                    } else {
                        // Fallback: treat as plain comma-separated? Or log warning
                        System.err.println("Unexpected calAttendees format: " + calAttendeesJson);
                    }
                }
//                if (calAttendeesJson == null || calAttendeesJson.isBlank()) {
//                    continue;
//                }
//
//                JSONObject json = new JSONObject(calAttendeesJson);
//                JSONArray attendees = json.getJSONArray("attendees");
//
//                for (int i = 0; i < attendees.length(); i++) {
//                    oldAttendeeSet.add(attendees.getString(i).toLowerCase());
//                }
            }
            for (String email : newAttendeeSet) {
                if (oldAttendeeSet.contains(email.toLowerCase())) {
                    oldMeetingCount++;
                } else {
                    newMeetingCount++;
                }
            }
            performance.setNewMettings(newMeetingCount);
            performance.setOldMettings(oldMeetingCount);
            performance.setCustomers(customer);
            this.calendarRepository.save(calendar);

            performance.setCalendar(calendar);
            this.performanceRepository.save(performance);

            Integer calId = calendar.getId();

            List<CalendarDetails> calendarDetails = this.calendarDetailsRepository.findCalendarDetailsList(calId);
            if (!calendarDetails.isEmpty()) {
                for (CalendarDetails list : calendarDetails) {
                    if (cladTypeList.contains(list.getCaldType())) {
                        if (list.getCaldType().equals("google")
                                && list.getCaldSycId().length() > 0
                                && list.getCaldSycId() != null) {
                            try {
                                googleCalendarDto.setCaldSycId(list.getCaldSycId());
                                innerResBody = new HashMap<>();
                                innerResBody = this.googleCalendarService.saveEvent(cusId, googleCalendarDto);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (list.getCaldType().equals("outlook")
                                && list.getCaldSycId().length() > 0
                                && list.getCaldSycId() != null) {
                            try {
                                outlookCalendarDto.setCaldSycId(list.getCaldSycId());
                                innerResBody = new HashMap<>();
                                innerResBody = outlookCalendarService.saveEvent(cusId, outlookCalendarDto);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        cladTypeList.remove(list.getCaldType());
                    } else {
                        try {
                            if (list.getCaldType().equals("google")
                                    && list.getCaldSycId().length() > 0
                                    && list.getCaldSycId() != null) {
                                try {
                                    this.googleCalendarService.deleteEvent(cusId, list.getCaldSycId());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (list.getCaldType().equals("outlook")
                                    && list.getCaldSycId().length() > 0
                                    && list.getCaldSycId() != null) {
                                try {
                                    outlookCalendarService.deleteEvent(cusId, list.getCaldSycId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            this.calendarDetailsRepository.deleteById(list.getId());

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            if (!cladTypeList.isEmpty()) {
                for (String ct : cladTypeList) {
                    CalendarDetails cd = new CalendarDetails();
                    if (ct.equals("google")) {
                        try {
                            innerResBody = new HashMap<>();
                            googleCalendarDto.setCaldSycId("");
                            innerResBody = this.googleCalendarService.saveEvent(cusId, googleCalendarDto);
                            if (innerResBody.get("error").equals("")) {
                                cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (ct.equals("outlook")) {
                        try {
                            innerResBody = new HashMap<>();
                            outlookCalendarDto.setCaldSycId("");
                            innerResBody = outlookCalendarService.saveEvent(cusId, outlookCalendarDto);
                            if (innerResBody.get("error").equals("")) {
                                cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cd.setCalendar(calendar);
                    cd.setCaldType(ct);
                    this.calendarDetailsRepository.save(cd);
                }
            }

            // Repeat Functionality Start
            if (!calendarDto.getCalRepeatType().equals("donotrepeat")) {
                List<Calendar> calendarList = null;
                if (calendarDto.getId() == null) {
                    if (calendarDto.getCalRepeatEveryType().equals("year")) {
                        repeatEveryTypeYearFun(cusId, calId);
                    }
                    if (calendarDto.getCalRepeatEveryType().equals("month")) {
                        repeatEveryTypeMonthFun(cusId, calId);
                    }
                    if (calendarDto.getCalRepeatEveryType().equals("day")) {
                        repeatEveryTypeDayFun(cusId, calId);
                    }
                    if (calendarDto.getCalRepeatEveryType().equals("week")) {
                        repeatEveryTypeWeekFun(cusId, calId);
                    }
                    calendarList = calendarRepository.findCalendarListByParentId(cusId, calId);
                    // Add Event Google And Outlook Start
                    addEditEventGoogleAndOutlook(cusId, calendarList, cladTypeListNew, calAttendees, timeZone);
                    // Add Event Google And Outlook End
                } else {
                    if (calendarDto.getEditAll().equals("Y")) {
                        Calendar mainRecord = calendarRepository.findCalendarById(calId, cusId);
                        List<Calendar> calList = null;
                        if (calendar.getCalParentId() != null && calendar.getCalParentId() > 0) {
                            calList = calendarRepository.findAllDataByCalIdAndParentId(cusId, calendar.getId(), calendar.getCalParentId());
                        } else {
                            calList = calendarRepository.findAllDataByCalIdWithOutParentId(cusId, calendar.getId());
                        }

                        for (Calendar cal : calList) {
                            cal.setCalTitle(mainRecord.getCalTitle());
                            cal.setCalDescription(mainRecord.getCalDescription());
                            cal.setCalAllDay(mainRecord.getCalAllDay());
                            cal.setCalTimeZone(mainRecord.getCalTimeZone());
                            cal.setCalAttendees(mainRecord.getCalAttendees());
                            cal.setCalNumbers(mainRecord.getCalNumbers());
                            cal.setCalRepeatEvery(mainRecord.getCalRepeatEvery());
                            cal.setCalRepeatType(mainRecord.getCalRepeatType());
                            cal.setCalRepeatEveryType(mainRecord.getCalRepeatEveryType());
                            cal.setCalRepeatDayName(mainRecord.getCalRepeatDayName());
                            cal.setCalRepeatSelectedOption(mainRecord.getCalRepeatSelectedOption());
                            cal.setCalUpdatedDateTime(mainRecord.getCalUpdatedDateTime());

                            String startDate = this.commonService.dateObjectToDbDateTime(cal.getCalStartDateTime());
                            String mainStartDate = this.commonService.dateObjectToDbDateTime(mainRecord.getCalStartDateTime());
                            cal.setCalStartDateTime(this.commonService.convertDate(startDate.split(" ")[0] + " " + mainStartDate.split(" ")[1]));

                            String endDate = this.commonService.dateObjectToDbDateTime(cal.getCalEndDateTime());
                            String mainEndDate = this.commonService.dateObjectToDbDateTime(mainRecord.getCalEndDateTime());
                            cal.setCalEndDateTime(this.commonService.convertDate(endDate.split(" ")[0] + " " + mainEndDate.split(" ")[1]));

                            this.calendarRepository.save(cal);
                        }

                        if (calendar.getCalParentId() != null && calendar.getCalParentId() > 0) {
                            calendarList = this.calendarRepository.findAllDataByCalIdAndParentId(cusId, calendar.getId(), calendar.getCalParentId());
                        } else {
                            calendarList = this.calendarRepository.findAllDataByCalIdWithOutParentId(cusId, calendar.getId());
                        }

                        // Add Event Google And Outlook Start
                        addEditEventGoogleAndOutlook(cusId, calendarList, cladTypeListNew, calAttendees, timeZone);
                        // Add Event Google And Outlook End
                    }
                }
            }
            // Repeat Functionality End

            // add meetings
            if (calendarDto.getOppId() != null) {
                MeetingsDto meetingsDto = new MeetingsDto();
                meetingsDto.setId(calendarDto.getMeetingId() != null ? calendarDto.getMeetingId() : null);
                meetingsDto.setCalendarId(calendar.getId());
                meetingsDto.setOppId(calendarDto.getOppId());
                meetingsDto.setCusId(cusId);
                meetingsDto.setContactIds(calendarDto.getContactIds());
                this.meetingsService.createMeeting(meetingsDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return resBody;
    }

    @Override
    public void deleteEvent(Integer cusId, DeleteEventDto deleteEventDto) {
        try {
            if (deleteEventDto.getCalId() > 0) {
                try {
                    List<CalendarDetails> calendarDetails = null;
                    if (deleteEventDto.getDeleteAll().equals("N")) {
                        calendarDetails = this.calendarDetailsRepository.findCalendarDetailsList(deleteEventDto.getCalId());
                    } else {
                        List<Integer> deleteIds = null;
                        if (deleteEventDto.getCalParentId() > 0) {
                            deleteIds = this.calendarRepository.findIdsAndParentId(cusId, deleteEventDto.getCalId(), deleteEventDto.getCalParentId());
                        } else {
                            deleteIds = this.calendarRepository.findIds(deleteEventDto.getCalId());
                        }
                        if (deleteIds.size() > 0) {
                            calendarDetails = this.calendarDetailsRepository.findCDsWithParentIdList(deleteIds);
                        }
                    }
                    if (calendarDetails != null) {
                        for (CalendarDetails cd : calendarDetails) {
                            if (deleteEventDto.getGoogleCalendar() == true
                                    && cd.getCaldType().equals("google")
                                    && cd.getCaldSycId().length() > 0) {
                                try {
                                    googleCalendarService.deleteEvent(cusId, cd.getCaldSycId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (deleteEventDto.getOutlookCalendar() == true
                                    && cd.getCaldType().equals("outlook")
                                    && cd.getCaldSycId().length() > 0) {
                                try {
                                    this.outlookCalendarService.deleteEvent(cusId, cd.getCaldSycId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            this.calendarDetailsRepository.deleteById(cd.getId());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Customers customers = this.customersRepository.findById(cusId).orElseThrow(() -> new RuntimeException("Customer not found"));
                Calendar calendar = calendarRepository.findCalendarById(deleteEventDto.getCalId(), cusId);

                String calAttendees = calendar.getCalAttendees();
                if (calAttendees == null) {
                    calAttendees = "";
                }

                this.sendCalendarDeleteEventEmail(customers, calendar, calAttendees, siteUrl, companyName);

                if (deleteEventDto.getDeleteAll().equals("N")) {
                    calendarRepository.deleteByCalId(deleteEventDto.getCalId(), cusId);
                } else {
                    if (deleteEventDto.getCalParentId() > 0) {
                        calendarRepository.deleteByCalIdWithParentId(deleteEventDto.getCalId(), deleteEventDto.getCalParentId(), cusId);
                    } else {
                        calendarRepository.deleteByCalIdWithOutParentId(deleteEventDto.getCalId(), cusId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void addEditEventGoogleAndOutlook(Integer cusId, List<Calendar> calendarList, List<String> cladTypeListNew, List<String> calAttendees, String timeZone) {
        Map<String, Object> innerResBody = new HashMap<>();
        for (Calendar calendar : calendarList) {
            try {
                List<String> cladTypeList = new ArrayList<>();
                for (String s : cladTypeListNew) {
                    cladTypeList.add(s);
                }
                GoogleCalendarDto googleCalendarDto = null;
                if (cladTypeList.contains("google")) {
                    googleCalendarDto = new GoogleCalendarDto();
                    googleCalendarDto.setCalTitle(calendar.getCalTitle());
                    googleCalendarDto.setCalDescription(calendar.getCalDescription());
                    try {
                        googleCalendarDto.setCalStartDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.dateObjectToDisplayDate(calendar.getCalStartDateTime()), timeZone, calendar.getCalTimeZone()));
                        googleCalendarDto.setCalEndDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.dateObjectToDisplayDate(calendar.getCalEndDateTime()), timeZone, calendar.getCalTimeZone()));
                    } catch (ParseException ee) {
                        ee.printStackTrace();
                        throw new RuntimeException();
                    }
                    googleCalendarDto.setCalAllDay(calendar.getCalAllDay());
                    googleCalendarDto.setCalTimeZone(calendar.getCalTimeZone());
                    googleCalendarDto.setCalAttendees(calAttendees);
                }

                OutlookCalendarDto outlookCalendarDto = null;
                if (cladTypeList.contains("outlook")) {
                    outlookCalendarDto = new OutlookCalendarDto();
                    outlookCalendarDto.setCalTitle(calendar.getCalTitle());
                    outlookCalendarDto.setCalDescription(calendar.getCalDescription());
                    try {
                        outlookCalendarDto.setCalStartDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.dateObjectToDisplayDate(calendar.getCalStartDateTime()), timeZone, calendar.getCalTimeZone()));
                        outlookCalendarDto.setCalEndDateTime(this.commonService.convertEventTimeZoneToUser(this.commonService.dateObjectToDisplayDate(calendar.getCalEndDateTime()), timeZone, calendar.getCalTimeZone()));
                    } catch (ParseException ee) {
                    }
                    outlookCalendarDto.setCalAllDay(calendar.getCalAllDay());
                    outlookCalendarDto.setCalTimeZone(calendar.getCalTimeZone());
                    outlookCalendarDto.setCalAttendees(calAttendees);
                }

                List<CalendarDetails> calendarDetails = calendarDetailsRepository.findCalendarDetailsList(calendar.getId());
                if (!calendarDetails.isEmpty()) {
                    for (CalendarDetails list : calendarDetails) {
                        if (cladTypeList.contains(list.getCaldType())) {
                            if (list.getCaldType().equals("google")
                                    && list.getCaldSycId().length() > 0
                                    && list.getCaldSycId() != null) {
                                try {
                                    googleCalendarDto.setCaldSycId(list.getCaldSycId());
                                    innerResBody = new HashMap<>();
                                    innerResBody = googleCalendarService.saveEvent(cusId, googleCalendarDto);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                            if (list.getCaldType().equals("outlook")
                                    && list.getCaldSycId().length() > 0
                                    && list.getCaldSycId() != null) {
                                try {
                                    outlookCalendarDto.setCaldSycId(list.getCaldSycId());
                                    innerResBody = new HashMap<>();
                                    innerResBody = outlookCalendarService.saveEvent(cusId, outlookCalendarDto);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                            cladTypeList.remove(list.getCaldType());
                        } else {
                            try {
                                if (list.getCaldType().equals("google")
                                        && list.getCaldSycId().length() > 0
                                        && list.getCaldSycId() != null) {
                                    try {
                                        googleCalendarService.deleteEvent(cusId, list.getCaldSycId());
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (list.getCaldType().equals("outlook")
                                        && list.getCaldSycId().length() > 0
                                        && list.getCaldSycId() != null) {
                                    try {
                                        outlookCalendarService.deleteEvent(cusId, list.getCaldSycId());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                }
                                calendarDetailsRepository.deleteById(list.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                if (!cladTypeList.isEmpty()) {
                    for (String ct : cladTypeList) {
                        CalendarDetails cd = new CalendarDetails();
                        if (ct.equals("google")) {
                            try {
                                innerResBody = new HashMap<>();
                                googleCalendarDto.setCaldSycId("");
                                innerResBody = googleCalendarService.saveEvent(cusId, googleCalendarDto);
                                if (innerResBody.get("error").equals("")) {
                                    cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        if (ct.equals("outlook")) {
                            try {
                                innerResBody = new HashMap<>();
                                outlookCalendarDto.setCaldSycId("");
                                innerResBody = outlookCalendarService.saveEvent(cusId, outlookCalendarDto);
                                if (innerResBody.get("error").equals("")) {
                                    cd.setCaldSycId(innerResBody.get("caldSycId").toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        cd.setCalendar(calendar);
                        cd.setCaldType(ct);
                        this.calendarDetailsRepository.save(cd);
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
                throw new RuntimeException(ee);
            }
        }
    }

    private void repeatEveryTypeYearFun(Integer cusId, Integer calId) {
        try {
            Calendar calendar = calendarRepository.findCalendarById(calId, cusId);
            LocalDateTime startDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatDate()));

            Map<String, Integer> getSplitDate = new HashMap<>();
            if (calendar.getCalRepeatSelectedOption() == 2 || calendar.getCalRepeatSelectedOption() == 3) {
                getSplitDate = this.commonService.getSplitDate(this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[0]);
            }

            LocalDateTime endDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatEndDate()));

            LocalDateTime endEventDate = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalEndDateTime()));

            int year = 0;
            if (!getSplitDate.isEmpty()) {
                year = getSplitDate.get("year") + 1;
            }

            LocalDateTime startDate = startDateTime;
            int dayNo = startDate.getDayOfMonth();
            startDate = startDate.plusYears(1);
            while (startDate.isBefore(endDateTime) || startDate.isEqual(endDateTime)) {
                if (calendar.getCalRepeatSelectedOption() == 1) {
                    startDate = this.commonService.changeDate(startDate, dayNo);
                }

                Calendar newCalendar = new Calendar();
                BeanUtils.copyProperties(calendar, newCalendar);
                // IMPORTANT: BeanUtils copies the ID also. If we don't null it, JPA will UPDATE the same row repeatedly.
                // We want a brand-new child row for each occurrence.
                newCalendar.setId(null);
                newCalendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalNotification("N");
                newCalendar.setCalParentId(calendar.getId());
                if (calendar.getCalRepeatSelectedOption() == 1) {
                    String dateStart = this.commonService.convertDateTimeObjectToDBDateTime(startDate.toString());
                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart.split(" ")[0] + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));
                } else if (calendar.getCalRepeatSelectedOption() == 2) {
                    String dateStart = this.commonService.getDateTime(year, Month.of(getSplitDate.get("month")), getSplitDate.get("day"), getSplitDate.get("week"));

                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[1]));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));

                    year++;
                } else if (calendar.getCalRepeatSelectedOption() == 3) {
                    String dateStart = this.commonService.getLastWeek(year, Month.of(getSplitDate.get("month")), getSplitDate.get("day"));

                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[1]));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));

                    year++;
                }
                this.calendarRepository.save(newCalendar);

                startDate = startDate.plusYears(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void repeatEveryTypeMonthFun(Integer cusId, Integer calId) {
        try {
            Calendar calendar = calendarRepository.findCalendarById(calId, cusId);
            int repeatEvery = calendar.getCalRepeatEvery();

            LocalDateTime startDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatDate()));

            Map<String, Integer> getSplitDate = new HashMap<>();
            if (calendar.getCalRepeatSelectedOption() == 2 || calendar.getCalRepeatSelectedOption() == 3) {
                getSplitDate = this.commonService.getSplitDate(this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[0]);
            }

            LocalDateTime endDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatEndDate()));

            LocalDateTime endEventDate = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalEndDateTime()));

            LocalDateTime startDate = startDateTime;
            int dayNo = startDate.getDayOfMonth();
            startDate = startDate.plusMonths(repeatEvery);
            while (startDate.isBefore(endDateTime) || startDate.isEqual(endDateTime)) {
                if (calendar.getCalRepeatSelectedOption() == 1) {
                    startDate = this.commonService.changeDate(startDate, dayNo);
                }

                Calendar newCalendar = new Calendar();
                BeanUtils.copyProperties(calendar, newCalendar);
                // IMPORTANT: BeanUtils copies the ID also. If we don't null it, JPA will UPDATE the same row repeatedly.
                // We want a brand-new child row for each occurrence.
                newCalendar.setId(null);
                newCalendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalNotification("N");
                newCalendar.setCalParentId(calendar.getId());
                if (calendar.getCalRepeatSelectedOption() == 1) {
                    String dateStart = this.commonService.convertDateTimeObjectToDBDateTime(startDate.toString());
                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart.split(" ")[0] + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));
                } else if (calendar.getCalRepeatSelectedOption() == 2) {
                    String dateStart = this.commonService.getDateTime(this.commonService.convertDateTimeObjectToReturnYear(startDate.toString()), Month.of(this.commonService.convertDateTimeObjectToReturnMonth(startDate.toString())), getSplitDate.get("day"), getSplitDate.get("week"));

                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[1]));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));
                } else if (calendar.getCalRepeatSelectedOption() == 3) {
                    String dateStart = this.commonService.getLastWeek(this.commonService.convertDateTimeObjectToReturnYear(startDate.toString()), Month.of(this.commonService.convertDateTimeObjectToReturnMonth(startDate.toString())), getSplitDate.get("day"));

                    newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(startDateTime.toString()).split(" ")[1]));
                    newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));
                }

                newCalendar = calendarRepository.save(newCalendar);
                startDate = startDate.plusMonths(repeatEvery);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void repeatEveryTypeDayFun(Integer cusId, Integer calId) {
        try {
            Calendar calendar = calendarRepository.findCalendarById(calId, cusId);
            int repeatEvery = calendar.getCalRepeatEvery();

            LocalDateTime startDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatDate()));

            LocalDateTime endDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatEndDate()));

            LocalDateTime endEventDate = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalEndDateTime()));

            for (LocalDateTime startDate = startDateTime.plusDays(repeatEvery); startDate.isBefore(endDateTime.plusDays(1)); startDate = startDate.plusDays(repeatEvery)) {
                Calendar newCalendar = new Calendar();
                BeanUtils.copyProperties(calendar, newCalendar);
                // IMPORTANT: BeanUtils copies the ID also. If we don't null it, JPA will UPDATE the same row repeatedly.
                // We want a brand-new child row for each occurrence.
                newCalendar.setId(null);
                newCalendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                newCalendar.setCalNotification("N");
                newCalendar.setCalParentId(calendar.getId());

                String dateStart = this.commonService.convertDateTimeObjectToDBDateTime(startDate.toString());
                newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart));
                newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart.split(" ")[0] + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));

                newCalendar = calendarRepository.save(newCalendar);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void repeatEveryTypeWeekFun(Integer cusId, Integer calId) {
        try {
            Calendar calendar = calendarRepository.findCalendarById(calId, cusId);
            int repeatEvery = calendar.getCalRepeatEvery();
            String[] weekDayNameList = calendar.getCalRepeatDayName().split(",");

            LocalDateTime startTempDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalStartDateTime()));

            LocalDateTime startDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatDate()));

            LocalDateTime endDateTime = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalRepeatEndDate()));

            LocalDateTime endEventDate = this.commonService.covertLocalDateTime(this.commonService.dateObjectToDbDateTime(calendar.getCalEndDateTime()));

            for (LocalDateTime startDate = startDateTime.plusWeeks(repeatEvery); startDate.isBefore(endDateTime.plusWeeks(1)); startDate = startDate.plusWeeks(repeatEvery)) {

                Map<String, Integer> getSplitDate = this.commonService.getSplitDateOnlyWeek(this.commonService.convertDateTimeObjectToDBDateTime(startDate.toString()).split(" ")[0]);

                for (String dayName : weekDayNameList) {
                    String dateStart = this.commonService.getDate(dayName.trim(), getSplitDate.get("week"), getSplitDate.get("month"), getSplitDate.get("year"));

                    int result = this.commonService.dateCampare(dateStart, this.commonService.convertDateTimeObjectToDBDateTime(endDateTime.toString()).split(" ")[0]);

                    if (result <= 0) {
                        Calendar newCalendar = new Calendar();
                        BeanUtils.copyProperties(calendar, newCalendar);
                        // IMPORTANT: BeanUtils copies the ID also. If we don't null it, JPA will UPDATE the same row repeatedly.
                        // We want a brand-new child row for each occurrence.
                        newCalendar.setId(null);
                        newCalendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                        newCalendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                        newCalendar.setCalNotification("N");
                        newCalendar.setCalParentId(calendar.getId());
                        newCalendar.setCalStartDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(startTempDateTime.toString()).split(" ")[1]));
                        newCalendar.setCalEndDateTime(this.commonService.convertDate(dateStart + " " + this.commonService.convertDateTimeObjectToDBDateTime(endEventDate.toString()).split(" ")[1]));

                        this.calendarRepository.save(newCalendar);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a calendar deletion / cancellation email to all relevant recipients.
     *
     * @param member       the member (invitee) whose event is canceled
     * @param calendar     the calendar event being canceled
     * @param calAttendees JSON string of additional attendee emails
     * @param imageSiteUrl base URL for images
     * @param companyName  company name
     */
    private void sendCalendarDeleteEventEmail(Customers member, Calendar calendar, String calAttendees, String imageSiteUrl, String companyName) throws ParseException {

        // 1. Prepare data similar to original model
        String calStartDT = this.commonService.dbToDdTimeZone(calendar.getCalStartDateTime().toString().substring(0, 19), calendar.getCalTimeZone());
        String firstName = member.getFirstName();
        String lastName = member.getLastName();

        String dateTime = this.commonService.dbDateToDisplayDateTime(calStartDT);
//        String webConferenceHtml = "";

        // 2. Build the HTML email body (inline style similar to the welcome email example)
        int currentYear = java.time.Year.now().getValue();
        String subject = "Canceled Event";

        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Canceled Event</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { text-align: center; padding: 20px 0; border-bottom: 2px solid #eee; }" +
                ".logo { max-width: 180px; height: auto; }" +
                ".content { padding: 20px 0; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.9em; color: #777; text-align: center; }" +
                ".event-detail { background: #f9f9f9; padding: 15px; border-radius: 8px; margin: 15px 0; }" +
                "a { color: #44288E !important; text-decoration: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<img src=\"" + imageSiteUrl + "images/logo/360Pipe_logo.png\" alt=\"Site Logo\" class=\"logo\">" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Dear " + firstName + " " + lastName + ",</p>" +
                "<p>We regret to inform you that the following event has been <strong>canceled</strong>:</p>" +
                "<div class=\"event-detail\">" +
                "<p><strong>Date & Time:</strong> " + dateTime + " (" + calendar.getCalTimeZone() + ")</p>" +
//                "<p><strong>Meeting Link:</strong> " + webConferenceHtml + "</p>" +
                "</div>" +
//                "<div style=\"text-align: center; margin: 20px 0;\">" + customerLogo + "</div>" +
                "<p>If you have any questions, please contact us at <a href=\"mailto:360pipeinc@gmail.com\">360pipeinc@gmail.com</a>.</p>" +
                "<p>Thank you for your understanding.</p>" +
                "<p>The " + companyName + " Team</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>&copy; " + currentYear + " " + companyName + ". All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        // 3. Send to primary email
        String memEmail = member.getEmailAddress();
        this.commonService.sendEmail(memEmail, subject, body, true);

        // 4. Send to Google Calendar email if present
        if (member.getGoogleCalendarEmail() != null) {
            String googleCalendarEmail = member.getGoogleCalendarEmail();
            this.commonService.sendEmail(googleCalendarEmail, subject, body, true);
        }

        // 5. Send to Outlook Calendar email if present
        if (member.getOutlookCalendarEmail() != null) {
            String outlookCalendarEmail = member.getOutlookCalendarEmail();
            this.commonService.sendEmail(outlookCalendarEmail, subject, body, true);
        }

        // 6. Send to additional attendees from JSON array
        // 6. Send to additional attendees from JSON array
        if (calAttendees != null && !calAttendees.trim().isEmpty()) {
            try {
                // Parse as JSON array since DB stores ["email1","email2"]
                JSONArray attendeesArray = new JSONArray(calAttendees.trim());
                for (int i = 0; i < attendeesArray.length(); i++) {
                    String attendeeEmail = attendeesArray.getString(i).toLowerCase();
                    this.commonService.sendEmail(attendeeEmail, subject, body, true);
                }
            } catch (JSONException e) {
                // Fallback: if it's actually an object like {"attendees":[...]}
                try {
                    JSONObject data = new JSONObject(calAttendees);
                    JSONArray attendees = data.getJSONArray("attendees");
                    for (int i = 0; i < attendees.length(); i++) {
                        String attendeeEmail = attendees.getString(i).toLowerCase();
                        this.commonService.sendEmail(attendeeEmail, subject, body, true);
                    }
                } catch (JSONException ex) {
                    System.err.println("Invalid calAttendees format: " + calAttendees);
                }
            }
        }
    }
}
