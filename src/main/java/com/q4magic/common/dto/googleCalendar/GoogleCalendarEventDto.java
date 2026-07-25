package com.q4magic.common.dto.googleCalendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarEventDto {
    private String id;
    private String summary;
    private String description;
    private String location;
    private ZonedDateTime start;
    private ZonedDateTime end;

}
