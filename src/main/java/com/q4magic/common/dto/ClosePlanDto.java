package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ClosePlanDto {
    private Integer id;
    private Integer oppId;
    private Integer cusId;
    private Integer contactId;
    private String contactName;
    private String date;
    private String status;
    private String statusTime;
    private String url;
    private Boolean hasComment;
}