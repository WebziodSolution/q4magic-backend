package com.q4magic.common.dto;

import lombok.Data;

@Data
public class CalendarAppointmentEventTypeDto {
    private Integer id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Integer durationHours;
    private Integer cusId;
    private String createdDate;
}
