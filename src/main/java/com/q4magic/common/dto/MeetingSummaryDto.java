package com.q4magic.common.dto;

import lombok.Data;

@Data
public class MeetingSummaryDto {
    private Integer id;
    private String transcript;
    private String summary;
    private Integer opportunityId;
    private Integer cusId;
    private String oppName;
    private String date;
}
