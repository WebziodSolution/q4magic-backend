package com.q4magic.calendar.service;

import com.q4magic.common.dto.CalendarDto;
import com.q4magic.common.dto.DeleteEventDto;

import java.util.Map;

public interface CalendarService {
    Map<String, Object> getEventList(Integer cusId, String calStartDateTime, String calEndDateTime, String cusTimeZone);

    Map<String, Object> getEvent(Integer cusId, Integer calId, String cusTimeZone);

    Map<String, Object> getSync(Integer cusId, String timeZone);

    Map<String, Object> saveEvent(Integer cusId, CalendarDto calendarDto);

    void deleteEvent(Integer cusId, DeleteEventDto deleteEventDto);
}
