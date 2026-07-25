package com.q4magic.meetings.serviceImpl;

import com.q4magic.common.dto.CalendarDto;
import com.q4magic.common.dto.MeetingsDto;
import com.q4magic.common.models.Calendar;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Meetings;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.repository.CalendarRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.MeetingsRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.meetings.service.MeetingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service(value = "MeetingsService")
public class MeetingsServiceImpl implements MeetingsService {

    @Value("${companyName}")
    String companyName;

    @Value("${server.database.timezone}")
    String serverDatabaseTimeZone;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<MeetingsDto> getAllMeetingsByOppId(Integer oppId, String cusTimeZone) {
        try {
            String timeZone = serverDatabaseTimeZone;

            List<Meetings> meetingsList = this.meetingsRepository.findByOppId(oppId);
            List<MeetingsDto> meetingsDtoList = new ArrayList<>();
            if (!meetingsList.isEmpty()) {
                for (Meetings meetings : meetingsList) {
                    MeetingsDto meetingsDto = new MeetingsDto();
                    meetingsDto.setId(meetings.getId());
                    meetingsDto.setOppId(meetings.getOpportunities().getId());
                    meetingsDto.setCalendarId(meetings.getCalendar().getId());
                    meetingsDto.setCusId(meetings.getCustomers().getId());

                    Calendar calendar = this.calendarRepository.findById(meetingsDto.getCalendarId()).orElseThrow(() -> new Exception("Calendar not found"));
                    CalendarDto calendarDto = new CalendarDto();
                    calendarDto.setId(calendar.getId());
                    calendarDto.setTitle(calendar.getCalTitle());
                    calendarDto.setDescription(calendar.getCalDescription());
                    calendarDto.setCustomerId(calendar.getCustomers().getId());
                    String displayTimeZone = cusTimeZone;
                    if (calendar.getCalType() != null && calendar.getCalType().equals(companyName.toLowerCase().replace(" ", ""))) {
                        displayTimeZone = calendar.getCalTimeZone();
                    }
                    calendarDto.setCalTimeZone(displayTimeZone);

                    try {
                        if (Objects.nonNull(calendar.getCalStartDateTime())) {
                            calendarDto.setStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, displayTimeZone));
                        }
                        if (Objects.nonNull(calendar.getCalEndDateTime())) {
                            calendarDto.setEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, displayTimeZone));
                        }
                        if (Objects.nonNull(calendar.getCalStartDateTime())) {
                            calendarDto.setDisplayStart(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalStartDateTime().toString()), timeZone, displayTimeZone));
                        }
                        if (Objects.nonNull(calendar.getCalEndDateTime())) {
                            calendarDto.setDisplayEnd(this.commonService.convertEventTimeZoneToUser(this.commonService.displayDateTime(calendar.getCalEndDateTime().toString()), timeZone, displayTimeZone));
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
                        e.printStackTrace();
                    }
                    meetingsDto.setCalendarDto(calendarDto);
                    meetingsDto.setContactIds(meetings.getContactIds());
                    meetingsDtoList.add(meetingsDto);
                }
            }
            return meetingsDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createMeeting(MeetingsDto meetingsDto) {
        try {
            Meetings meetings = meetingsDto.getId() != null ? this.meetingsRepository.findById(meetingsDto.getId()).orElseThrow(() -> new RuntimeException("Meeting not found")) : new Meetings();
            if (meetingsDto.getId() == null) {
                Opportunities opportunities = this.opportunitiesRepository.findById(meetingsDto.getOppId()).orElseThrow(() -> new Exception("Opportunities not found"));
                Calendar calendar = this.calendarRepository.findById(meetingsDto.getCalendarId()).orElseThrow(() -> new Exception("Calendar not found"));
                Customers customers = this.customersRepository.findById(meetingsDto.getCusId()).orElseThrow(() -> new Exception("Customers not found"));

                meetings.setCalendar(calendar);
                meetings.setOpportunities(opportunities);
                meetings.setCustomers(customers);
                meetings.setCreatedAt(new Date());
            }
            meetings.setContactIds(meetingsDto.getContactIds());
            this.meetingsRepository.save(meetings);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMeeting(Integer meetingId) {
        try {
            Meetings meetings = this.meetingsRepository.findById(meetingId).orElseThrow(() -> new Exception("Meetings not found"));
            this.meetingsRepository.delete(meetings);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMeetingByCalendar(Integer calendarId) {
        try {
            Meetings meetingsList = this.meetingsRepository.findByCalendarId(calendarId);
            if (meetingsList != null) {
                this.meetingsRepository.delete(meetingsList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
