package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpportunityContactDto {
    private Integer id;
    private String salesforceOpportunityContactId;
    private Integer oppId;
    private Integer contactId;
    private String salesforceContactId;
    private String contactName;
    private String title;
    private String role;
    private Boolean isKey;
    private Boolean isDeleted;
    private List<OpportunityContactNotesDto> opportunityContactNotesList;
}
