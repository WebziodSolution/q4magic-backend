package com.q4magic.common.dto;

import lombok.Data;

@Data
public class MeetingsDto {
    private Integer id;
    private Integer oppId;
    private Integer cusId;
    private Integer calendarId;
    private CalendarDto calendarDto;
    private String contactIds;
}
