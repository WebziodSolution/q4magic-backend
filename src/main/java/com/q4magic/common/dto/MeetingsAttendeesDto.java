package com.q4magic.common.dto;

import lombok.Data;

@Data
public class MeetingsAttendeesDto {
    private Integer id;
    private Integer meetingId;
    private Integer contactId;
    private String contactName;
    private String title;
    private String role;
    private String note;
}
