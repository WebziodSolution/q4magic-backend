package com.q4magic.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SalesProcessDto {
    private Integer id;
    private Integer oppId;
    private String goLive;
    private String processDate;
    private String process;
    private String notes;
    private String reason;
    private Integer contactId;
}
