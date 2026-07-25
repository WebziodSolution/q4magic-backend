package com.q4magic.common.dto.googleCalendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleEventsResponse {
    private java.util.List<GoogleEventItem> items;
}

