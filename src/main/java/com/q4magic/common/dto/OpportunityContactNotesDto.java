package com.q4magic.common.dto;

import lombok.Data;

@Data
public class OpportunityContactNotesDto {
    private Integer id;
    private Integer opportunityContactId;
    private String type;
    private String note;
}
