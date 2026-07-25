package com.q4magic.calendar.calendarAppointment.serviceImpl;

import com.q4magic.calendar.calendarAppointment.service.CalendarAppointmentService;
import com.q4magic.common.dto.*;
import com.q4magic.common.googleCalendar.service.GoogleCalendarService;
import com.q4magic.common.models.*;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.util.ICS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Service(value = "CalendarAppointmentService")
public class CalendarAppointmentServiceImpl implements CalendarAppointmentService {
    @Value("${server.database.timezone}")
    String serverDatabaseTimeZone;

    @Value("${companyName}")
    String companyName;

    @Value("${icsdownload-dir}")
    private String icsDownloadPath;

    @Value("${siteNameBigCom}")
    String siteNameBigCom;

    @Value("${siteUrl}")
    String siteUrl;

    @Autowired
    private CalendarAppointmentAvailabilitySlotsRepository calendarAppointmentAvailabilitySlotsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private CalendarAppointmentEventTypeRepository calendarAppointmentEventTypeRepository;

    @Autowired
    private CalendarNotificationRepository calendarNotificationRepository;

    @Override
    public List<CalendarAppointmentAvailabilitySlotsDto> getAvailabilitySlotsList(Integer cusId) {
        try {
            List<CalendarAppointmentAvailabilitySlots> calendarAppointmentAvailabilitySlots = calendarAppointmentAvailabilitySlotsRepository.findByCustomerId(cusId);
            List<CalendarAppointmentAvailabilitySlotsDto> availabilitySlotsListResponseDtos = new ArrayList<>();
            if (!calendarAppointmentAvailabilitySlots.isEmpty()) {
                for (CalendarAppointmentAvailabilitySlots availabilitySlots : calendarAppointmentAvailabilitySlots) {
                    CalendarAppointmentAvailabilitySlotsDto availabilitySlotsDto = new CalendarAppointmentAvailabilitySlotsDto();
                    availabilitySlotsDto.setId(availabilitySlots.getId());
                    availabilitySlotsDto.setDayName(availabilitySlots.getDayName());
                    availabilitySlotsDto.setAvailable(availabilitySlots.getAvailable());
                    availabilitySlotsDto.setStartTime(availabilitySlots.getStartTime().toString());
                    availabilitySlotsDto.setEndTime(availabilitySlots.getEndTime().toString());
                    availabilitySlotsDto.setAvailable(availabilitySlots.getAvailable());
                    availabilitySlotsListResponseDtos.add(availabilitySlotsDto);
                }
            }
            return availabilitySlotsListResponseDtos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> saveAvailabilitySlots(Integer cusId,
                                                     List<CalendarAppointmentAvailabilitySlotsDto> list) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            for (CalendarAppointmentAvailabilitySlotsDto dto : list) {
                CalendarAppointmentAvailabilitySlots availabilitySlots = this.calendarAppointmentAvailabilitySlotsRepository.findById(dto.getId()).orElse(new CalendarAppointmentAvailabilitySlots());
                Customers customers = this.customersRepository.findById(dto.getCusId()).orElseThrow(() -> new RuntimeException("Customer Not Found"));
                availabilitySlots.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                availabilitySlots.setDayName(dto.getDayName());
                availabilitySlots.setAvailable(dto.getAvailable());

                availabilitySlots.setStartTime(Time.valueOf(dto.getStartTime()));
                availabilitySlots.setEndTime(Time.valueOf(dto.getEndTime()));
                availabilitySlots.setCustomers(customers);
                availabilitySlots.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                this.calendarAppointmentAvailabilitySlotsRepository.save(availabilitySlots);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", e.getMessage());
        }

        return resBody;
    }

    @Override
    public Map<String, Object> freeSlotList(String userTimeZone, FreeSlotListDto freeSlotListDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Customers customers = this.customersRepository.findById(freeSlotListDto.getSlotUserId()).orElseThrow(() -> new RuntimeException("Customer Not Found"));

            if (freeSlotListDto.getSlotDateTime().length() > 10) {
                freeSlotListDto.setSlotDateTime(this.commonService.convertEventTimeZoneToUser(freeSlotListDto.getSlotDateTime(), freeSlotListDto.getTimeZone(), serverDatabaseTimeZone));
            }

            String aasDayName = this.commonService.dayName(freeSlotListDto.getSlotDateTime());
            CalendarAppointmentAvailabilitySlots availabilitySlots = null;
            try {
                availabilitySlots = this.calendarAppointmentAvailabilitySlotsRepository.findSlot(customers.getId(), aasDayName);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            String startTime = "00:00:00";
            String endTime = "24:00:00";

            String aasAvailableYN = "Y";
            if (availabilitySlots != null) {
                startTime = availabilitySlots.getStartTime().toString();
                endTime = availabilitySlots.getEndTime().toString();
                aasAvailableYN = availabilitySlots.getAvailable();

                String memberTimeZone = userTimeZone;

                String tempStartDateTime = this.commonService.convertEventTimeZoneToUser(freeSlotListDto.getSlotDateTime().substring(0, 10) + " " + startTime, memberTimeZone, freeSlotListDto.getTimeZone());

                String tempEndDateTime = this.commonService.convertEventTimeZoneToUser(freeSlotListDto.getSlotDateTime().substring(0, 10) + " " + endTime, memberTimeZone, freeSlotListDto.getTimeZone());

                String[] tempStart = tempStartDateTime.split(" ");
                String[] tempEnd = tempEndDateTime.split(" ");
                if (tempStart[0].equals(tempEnd[0])) {
                    startTime = this.commonService.lastCharaters(tempStartDateTime, 8);
                    endTime = this.commonService.lastCharaters(tempEndDateTime, 8);
                } else {
                    startTime = this.commonService.lastCharaters(tempStartDateTime, 8);
                    endTime = "24:00:00";
                    if (!tempStart[0].equals(freeSlotListDto.getSlotDateTime().substring(0, 10))) {
                        startTime = this.commonService.remainingTime(freeSlotListDto.getSlotTimeMinus(), startTime, endTime);
                        endTime = this.commonService.lastCharaters(tempEndDateTime, 8);
                    }
                }
            }

            List<String> slotList = new ArrayList<>();
            if (aasAvailableYN.equals("Y")) {
                slotList = this.commonService.slotList(freeSlotListDto.getSlotTimeMinus(), startTime, endTime);
                // Current Date Slot List Start
                if (freeSlotListDto.getCurrentDateYN().equals("Y")) {
                    List<String> slotNewList = new ArrayList<>();
                    for (String sl : slotList) {
                        if (this.commonService.checkBigFirstTime(sl, this.commonService.currentTime(freeSlotListDto.getTimeZone()))) {
                            slotNewList.add(sl);
                        }
                    }
                    slotList = slotNewList;
                }

                String slotDateTime = "";
                try {
                    if (Objects.nonNull(freeSlotListDto.getSlotDateTime())) {
                        slotDateTime = this.commonService.dbDate(freeSlotListDto.getSlotDateTime());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    new RuntimeException(e);
                }
                List<com.q4magic.common.models.Calendar> bookSlotList = this.calendarRepository.findBookList(customers.getId(), slotDateTime);
                Boolean calAllDay = false;
                for (com.q4magic.common.models.Calendar bookSlot : bookSlotList) {
                    startTime = this.commonService.displayDbDateTimeToTime(this.commonService.convertEventTimeZoneToUserDB(bookSlot.getCalStartDateTime().toString().substring(0, 19), serverDatabaseTimeZone, freeSlotListDto.getTimeZone()).substring(0, 19));
                    endTime = this.commonService.displayDbDateTimeToTime(this.commonService.convertEventTimeZoneToUserDB(bookSlot.getCalEndDateTime().toString().substring(0, 19), serverDatabaseTimeZone, freeSlotListDto.getTimeZone()).substring(0, 19));
                    calAllDay = Boolean.valueOf(bookSlot.getCalAllDay());
                    if (calAllDay) {
                        break;
                    }
                    slotList = this.commonService.removeBookSlot(freeSlotListDto.getSlotTimeMinus(), slotList, startTime, endTime);
                }

                if (calAllDay) {
                    slotList = new ArrayList<>();
                }
            }
            resBody.put("freeSlotList", slotList);
        } catch (Exception e) {
            e.printStackTrace();
            new RuntimeException(e.getMessage());
        }
        return resBody;

    }

    @Override
    public Map<String, Object> saveAppointment(CalendarDto calendarDto) {
        Map<String, Object> resBody = new HashMap<>();
        Map<String, Object> innerResBody = new HashMap<>();
        resBody.put("error", "");
        try {
            Integer cusId = calendarDto.getCustomerId();
            innerResBody = this.googleCalendarService.getUserTimezone(cusId);
            String timeZone = null;
            if (innerResBody.get("calTimeZone") != null) {
                timeZone = innerResBody.get("calTimeZone").toString();
            }
            if (calendarDto.getCalTimeZone().equals("")) {
                if (timeZone != null) {
                    calendarDto.setCalTimeZone(timeZone);
                }
            }
            // Set Database Timezone : Not Delete
            timeZone = serverDatabaseTimeZone;

            try {
                if (Objects.nonNull(calendarDto.getStart())) {
                    calendarDto.setStart(this.commonService.dbDateTime(calendarDto.getStart()));
                }
                if (Objects.nonNull(calendarDto.getEnd())) {
                    calendarDto.setEnd(this.commonService.dbDateTime(calendarDto.getEnd()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String startDateTime = null;
            try {
                if (Objects.nonNull(calendarDto.getStart())) {
                    startDateTime = this.commonService.convertEventTimeZoneToUserDB(calendarDto.getStart(), calendarDto.getCalTimeZone(), timeZone);
                    startDateTime = this.commonService.displayDateTime(calendarDto.getStart());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Customers customers = this.customersRepository.findById(cusId).orElseThrow(() -> new RuntimeException("Customer not found!"));

            List<String> slotList = new ArrayList<>();

            FreeSlotListDto freeSlotListDto = new FreeSlotListDto();
            freeSlotListDto.setSlotDateTime(startDateTime);
            freeSlotListDto.setSlotUserId(calendarDto.getCustomerId());
            freeSlotListDto.setSlotTimeMinus(calendarDto.getSlotTimeMinus());
            freeSlotListDto.setTimeZone(calendarDto.getCalTimeZone());
            freeSlotListDto.setCurrentDateYN(calendarDto.getCurrentDateYN());
            innerResBody = freeSlotList(calendarDto.getCalTimeZone(), freeSlotListDto);
            slotList = (List<String>) innerResBody.get("freeSlotList");

            String checkSlotTimeValidation = this.commonService.convertDateTimeToTime(startDateTime);
            if (slotList.contains(checkSlotTimeValidation)) {
                Calendar calendar = new Calendar();
                calendar.setCalTitle("Meeting with " + calendarDto.getTitle());

                String webConference = "";
//                if (member.getWebConference() != null && !member.getWebConference().trim().isEmpty()) {
//                    webConference = "\n\n" + member.getWebConference();
//                }
                calendar.setCalDescription(calendarDto.getDescription() + webConference);

                calendar.setCustomers(customers);
                calendar.setCalAllDay("false");
                calendar.setCalTimeZone(calendarDto.getCalTimeZone());
                calendar.setCalAttendees(calendarDto.getCalAttendees());
                calendar.setCalAetId(Long.parseLong(calendarDto.getCalAetId().toString()));
                try {
                    if (Objects.nonNull(calendarDto.getStart())) {
                        calendar.setCalStartDateTime(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getStart(), calendarDto.getCalTimeZone(), timeZone)));
                    }
                    if (Objects.nonNull(calendarDto.getEnd())) {
                        calendar.setCalEndDateTime(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getEnd(), calendarDto.getCalTimeZone(), timeZone)));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                calendar.setCalCreatedDateTime(new Timestamp(System.currentTimeMillis()));
                calendar.setCalUpdatedDateTime(new Timestamp(System.currentTimeMillis()));
                calendar.setCalNotification("N");

                String numbers = "";
                for (String number : calendarDto.getContactList()) {
                    if (numbers.equals("")) {
                        numbers = number;
                    } else {
                        numbers += "," + number;
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
                calendar = calendarRepository.save(calendar);

                // member notification start
                try {
                    String emailNotification = customers.getEmailNotification();
                    if(emailNotification != null && !emailNotification.equals("")) {
                        for (String minutes:emailNotification.split(",")) {
                            CalendarNotification calendarNotification = new CalendarNotification();
                            calendarNotification.setCalendar(calendar);
                            calendarNotification.setMinutes(Long.valueOf(minutes));
                            calendarNotification.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                            calendarNotification.setNotification("N");
                            this.calendarNotificationRepository.save(calendarNotification);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // member notification end

                String calAtt = calendar.getCalAttendees();
                JSONObject data = new JSONObject(calAtt);
                JSONArray attendees = data.getJSONArray("attendees");
                String inviteeEmail = attendees.get(attendees.length() - 1).toString().toLowerCase();

                String guestsList = "";
                List<String> guestICSList = new ArrayList<>();
                guestICSList.add(inviteeEmail);
                for (int i = 0; i < attendees.length() - 1; i++) {
                    if (guestsList.equals("")) {
                        guestsList += attendees.get(i).toString().toLowerCase();
                    } else {
                        guestsList += "<br />" + attendees.get(i).toString().toLowerCase();
                    }
                    guestICSList.add(attendees.get(i).toString().toLowerCase());
                }
                String memEmail = customers.getEmailAddress();
                String firstName = customers.getFirstName();
                String lastName = customers.getLastName();
                String googleCalendarEmail = customers.getGoogleCalendarEmail();
                String outlookCalendarEmail = customers.getOutlookCalendarEmail();

                // ICS File Code Start
                String icsFileDescription = this.commonService.nl2br(calendarDto.getDescription() + webConference);

//                if(member.getWebConference() != null) {
//                    icsFileDescription += "<br /><br />Web Conference Link :<br />"+member.getWebConference();
//                }

                icsFileDescription += "<br /><br />Guest Email(s) :<br />" + inviteeEmail;
                if (!guestsList.equals("")) {
                    icsFileDescription += "<br />" + guestsList;
                }

                String smsNumberList = "";
                if (calendarDto.getContactList().size() > 0) {
                    for (int i = 0; i < calendarDto.getContactList().size(); i++) {
                        if (smsNumberList.equals("")) {
                            smsNumberList += calendarDto.getContactList().get(i);
                        } else {
                            smsNumberList += "<br />" + calendarDto.getContactList().get(i);
                        }
                    }
                }

                if (!smsNumberList.equals("")) {
                    icsFileDescription += "<br /><br />Mobile Number(s) :<br />" + smsNumberList;
                }

                ICSEventDto eventDto = new ICSEventDto();
                eventDto.setReplyToAdd(memEmail);
                eventDto.setStartDate(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getStart(), calendarDto.getCalTimeZone(), TimeZone.getDefault().getID())));
                eventDto.setEndDate(this.commonService.convertDate(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getEnd(), calendarDto.getCalTimeZone(), TimeZone.getDefault().getID())));
                eventDto.setSummary(calendar.getCalTitle());
//                eventDto.setDescription(this.commonService.findLinkAndReplace(this.commonService.br2nl(icsFileDescription)));
                eventDto.setDescription(this.commonService.br2nl(icsFileDescription));
                eventDto.setAttendees(guestICSList);
                eventDto.setMemberName(this.commonService.ucWords(firstName.trim() + " " + lastName.trim()));
                eventDto.setTimeZone(calendarDto.getCalTimeZone());
                String rootPath = icsDownloadPath + cusId + "/";
                File f = new File(rootPath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                String fileName = "CalendarInvite.ics";
                String filePath = rootPath + fileName;
                File myFile = new File(filePath);
                if (myFile.exists()) {
                    myFile.delete();
                }
                ICS.generateICSFile(eventDto, filePath, siteNameBigCom);


                String guestsHtml = "";
                if (guestsList != null && !guestsList.trim().isEmpty()) {
                    guestsHtml =
                            "<p><b>Guests:</b></p>" +
                                    "<p>" + guestsList + "<br /><br /></p>";
                }

                String comment = "";
                if (calendarDto.getDescription() != null && !calendarDto.getDescription().trim().isEmpty()) {
                    comment = "<p><b>Kindly see the details of the meeting.</b></p><p>" + calendarDto.getDescription() + "<br /><br /></p>";
                }
                int currentYear = java.time.Year.now().getValue();
                CalendarAppointmentEventType eventType = this.calendarAppointmentEventTypeRepository.findById(Integer.parseInt(calendarDto.getCalAetId().toString())).orElse(null);
                if (memEmail != null && !memEmail.equals("")) {
                    String subject = "New Event Meeting";
                    String body = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset=\"UTF-8\">" +
                            "<title>New Event Meeting</title>" +
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
                            "<img src=\"" + siteUrl + "images/logo/360Pipe_logo.png\" alt=\"Site Logo\" class=\"logo\">" +
                            "</div>" +
                            "<div class=\"content\">" +
                            "<p>Hi " + firstName + " " + lastName + ", A new meeting has been scheduled. </p>" +
                            "<p><b>Event Type:</b></p><p>" + eventType.getTitle() + "<br /><br /></p>" +
                            "<p><b>Invitee:</b></p><p>" + calendarDto.getTitle() + "<br /><br /></p>" +
                            "<p><b>Invitee Email:</b></p><p>" + inviteeEmail + "<br /><br /></p>" +
                            guestsHtml +
                            "<p><b>Invitee Date/Time:</b></p><p>" + this.commonService.dbDateToDisplayDateTime(calendarDto.getStart()) + "<br /><br /></p>" +
                            "<p><b>Invitee Timezone:</b></p><p>" + calendarDto.getCalTimeZone() + "<br /><br /></p>" +
                            "<p><b>Your Date/Time:</b></p><p>" + this.commonService.dbDateToDisplayDateTime(this.commonService.convertEventTimeZoneToUserDB(calendarDto.getStart(), calendarDto.getCalTimeZone(), calendarDto.getMemTimeZone())) + "<br /><br /></p>" +
                            "<p><b>Your Timezone:</b></p><p>" + calendarDto.getMemTimeZone() + "<br /><br /></p>" +
                            comment +
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
                    this.commonService.sendEmail(memEmail, subject, body, true);

                    if(!memEmail.equals(googleCalendarEmail)
                            && googleCalendarEmail != null) {
                        if(!googleCalendarEmail.equals("")) {
                            this.commonService.sendEmail(googleCalendarEmail, subject, body, true);
                        }
                    }

                    if(!memEmail.equals(outlookCalendarEmail)
                            && outlookCalendarEmail != null) {
                        if(!outlookCalendarEmail.equals("")) {
                            this.commonService.sendEmail(outlookCalendarEmail, subject, body, true);
                        }
                    }
                }

                for (int i = 0; i < attendees.length(); i++) {
                    String email = attendees.getString(i);
                    if (!email.equals(memEmail)) {
                        String subject = "New Event Meeting";
                        String body = "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<meta charset=\"UTF-8\">" +
                                "<title>New Event Meeting</title>" +
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
                                "<img src=\"" + siteUrl + "images/logo/360Pipe_logo.png\" alt=\"Site Logo\" class=\"logo\">" +
                                "</div>" +
                                "<div class=\"content\">" +
                                "<p>Hi " + firstName + " " + lastName + ", Your " + eventType.getTitle() + " with " + fileName + " " + lastName + " at " + this.commonService.dbDateToDisplayDateTime(calendarDto.getStart()) + " " + calendarDto.getCalTimeZone() + " is scheduled. </p>" +
                                comment +
                                guestsHtml +
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
                        this.commonService.sendEmailWithAttachment(email, subject, body, true, fileName, filePath);
                    }
                }
            } else {
                resBody.put("error", "1");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("error", "Invalid Data");
            throw new RuntimeException(e);
        }
        return resBody;
    }

    @Override
    public List<com.q4magic.common.models.Calendar> getMyCalendarAppointmentLis(Integer count, Integer cusId) {
        Date currentDate = new Date();
        Pageable pageable = PageRequest.of(0, count);
        List<Calendar> calenderList = this.calendarRepository.findUpcomingAppointmentList(cusId, currentDate, pageable);
        return calenderList;
    }

    @Override
    public Map<String, Object> sendEmailAppointmentLink(SendAppointmentLinkDto sendAppointmentLinkDto) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("error", "");
        try {
            for (String email : sendAppointmentLinkDto.getEmailsList()) {
                String subject = "Meeting Link";
                String body = sendAppointmentLinkDto.getMessage();
                this.commonService.sendEmail(email, subject, body, false);
            }
        } catch (Exception e) {
            resBody.put("error", "Invalid data");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return resBody;
    }
}
