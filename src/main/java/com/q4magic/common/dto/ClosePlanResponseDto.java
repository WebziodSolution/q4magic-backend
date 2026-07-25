package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ClosePlanResponseDto {
    private Integer closePlanId;
    private Integer oppId;
    private Integer contactId;
    private Integer createdBy;
    private String createdByName;
    private String contactName;
    private String oppName;
    private String businessValue;
    private String nextSteps;
    private String status;
    private List<SalesProcessDto> decisionMap;
    private List<OpportunityContactDto> opportunityContactDto;
    private List<DocsAttachmentsDto> docsAttachmentsDtoList;

}
