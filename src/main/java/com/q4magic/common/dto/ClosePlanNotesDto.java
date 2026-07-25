package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ClosePlanNotesDto {
    private Integer id;
    private Integer closePlanId;
    private Integer sendTo;
    private Integer createdBy;
    private String createdByName;
    private String comments;
    private String createdAt;
}