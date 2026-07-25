package com.q4magic.common.dto.googleCalendar;

import lombok.Data;

import java.util.List;

@Data
public class GoogleCalendarDto {
    private String calTitle;
    private String calDescription;
    private String location;
    private String calStartDateTime;
    private String calEndDateTime;
    private String caldSycId;
    private String calAllDay;
    private String calTimeZone;
    private List<String> calAttendees;
}
