package com.q4magic.common.dto.googleCalendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleEventItem {

    private String id;
    private String summary;
    private String description;
    private String location;
    // Google provides start and end fields with nested objects
    private GoogleEventDateTime start;
    private GoogleEventDateTime end;
}

