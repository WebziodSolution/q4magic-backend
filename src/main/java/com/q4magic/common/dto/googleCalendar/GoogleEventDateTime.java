package com.q4magic.common.dto.googleCalendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleEventDateTime {

    // For timed events (includes timezone)
    // Example: "2025-12-09T10:00:00-05:00"
    private String dateTime;

    // For full-day events
    // Example: "2025-12-25"
    private String date;
}
