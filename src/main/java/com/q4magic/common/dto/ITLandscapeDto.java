package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ITLandscapeDto {
    private Integer id;
    private String salesforceCompetitorId;
    private Integer customerId;
    private Integer opportunityId;
    private String salesforceOpportunityId;
    private String itCategory;
    private String subCategory;
    private String vendor;
    private String competitorFlag;
    private String partnerFlag;
    private Boolean isDeleted;
}
