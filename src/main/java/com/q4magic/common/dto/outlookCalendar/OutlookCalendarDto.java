package com.q4magic.common.dto.outlookCalendar;

import lombok.Data;

import java.util.List;

@Data
public class OutlookCalendarDto {
    private String calTitle;
    private String calDescription;
    private String calStartDateTime;
    private String calEndDateTime;
    private String caldSycId;
    private String calAllDay;
    private String calTimeZone;
    private List<String> calAttendees;
    private String location;
}
