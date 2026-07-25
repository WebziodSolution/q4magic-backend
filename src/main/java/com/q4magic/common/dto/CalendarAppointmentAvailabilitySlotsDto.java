package com.q4magic.common.dto;

import lombok.Data;

@Data
public class CalendarAppointmentAvailabilitySlotsDto {
    private Integer id;
    private String dayName;
    private String startTime;
    private String endTime;
    private Integer cusId;
    private String available;
}