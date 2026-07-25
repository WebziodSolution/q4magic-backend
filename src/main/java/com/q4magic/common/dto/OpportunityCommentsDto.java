package com.q4magic.common.dto;

import lombok.Data;

@Data
public class OpportunityCommentsDto {
    private Integer id;
    private Integer oppId;
    private Integer cusId;
    private String title;
    private String comment;
    private String commentDate;
}
