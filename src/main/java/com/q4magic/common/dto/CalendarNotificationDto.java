package com.q4magic.common.dto;

import lombok.Data;

@Data
public class CalendarNotificationDto {
    private Integer id;
    private Long minutes;
    private String notification;
    private Integer calendarId;
    private String createdDate;
    private String sendDateTime;
}
