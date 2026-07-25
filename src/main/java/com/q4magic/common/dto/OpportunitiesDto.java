package com.q4magic.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpportunitiesDto {
    private Integer id;
    private String salesforceOpportunityId;
    private Integer accountId;
    private String accountName;
    private String opportunity;
    private String salesStage;
    private Integer dealAmount;
    private Integer discountPercentage;
    private Integer listPrice;
    private String closeDate;
    private String nextSteps;
    private String whyDoAnything;
    private String currentEnvironment;
    private String decisionMap;
    private String businessValue;
    private String status;
    private String forecastDate;
    private String decisionCriteria;
    private String recordStatus;
    private Boolean isDeleted;
    private Integer createdBy;
    private String logo;
    private String newLogo;
    private String createdAt;
    private String domain;
    private List<OpportunityPartnerDetailsDto> opportunityPartnerDetails;
    private List<ImageResponseDto> opportunityDocs = new ArrayList<>();
    private List<ClosePlanDto>closePlanDtoList = new ArrayList<>();
    private List<ClosePlanNotesDto>closePlanCommentDto = new ArrayList<>();
}
