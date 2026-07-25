package com.q4magic.common.dto;

import lombok.Data;

@Data
public class OpportunityPartnerDetailsDto {
    private Integer id;
    private String salesforceOpportunityPartnerId;
    private Integer opportunityId;
    private String accountToId;
    private String accountId;
    private String accountName;
    private String role;
    private Boolean isPrimary;
    private Boolean isDeleted;
    private Integer createdBy;
}