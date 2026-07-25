package com.q4magic.opportunityPartnerDetails.service;

import com.q4magic.common.dto.OpportunityPartnerDetailsDto;

import java.util.List;

public interface OpportunityPartnerDetailsService {
    OpportunityPartnerDetailsDto getSalesforceOpportunityPartnerId(String id);

    List<OpportunityPartnerDetailsDto> getAllOpportunityPartnerDetails(Integer id);

    OpportunityPartnerDetailsDto getOpportunityPartnerDetailsById(Integer id);

    OpportunityPartnerDetailsDto createOpportunityPartnerDetails(OpportunityPartnerDetailsDto dto, Boolean syncToSalesforce);

    OpportunityPartnerDetailsDto updateOpportunityPartnerDetails(Integer id, OpportunityPartnerDetailsDto dto, Boolean syncToSalesforce);

    void deleteOpportunityPartnerDetails(Integer id, Boolean syncToSalesforce, Integer createdBy);
}
