package com.q4magic.common.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ICSEventDto {
    private String summary;
    private String description;
    private List<String> attendees;
    private Date startDate;
    private Date endDate;

    private String memberName;
    private String timeZone;
    private String replyToAdd;
}
