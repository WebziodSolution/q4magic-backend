package com.q4magic.oportunities.service;

import com.q4magic.common.dto.OpportunitiesDto;

import java.util.List;
import java.util.Map;

public interface OpportunitiesService {
    List<OpportunitiesDto> getAllOpportunities(Integer userId, String fetchType, String search, List<String> salesStages, List<String> status);

    List<Map<String, Object>> getAllOpportunitiesGroupedByStage(
            Integer userId,
            String fetchType,
            String search,
            List<String> salesStages,
            List<String> status
    );

    List<Map<String, Object>> getOpportunityOptions(Integer userId);

    OpportunitiesDto getOpportunityById(Integer id);

    OpportunitiesDto createOpportunity(OpportunitiesDto opportunity, Boolean syncToSalesforce);

    OpportunitiesDto updateOpportunity(Integer id, OpportunitiesDto opportunity, Boolean syncToSalesforce);

    void deleteOpportunity(Integer id, Boolean syncToSalesforce);

    boolean deleteOpportunityLogo(Integer id, Integer userId);

    void updateOpportunityDeal(Integer id, Integer dealAmount);

    String updateOpportunityLogo(Integer userId, Integer oppId, String image);

    Map<String,Object> updateOpportunityData(Map<String,Object> data);

    Map<String,Object> updateLastOpportunityData(Map<String,Object> data);

    Map<String,Object> checkOpportunity(Integer oppId);

    Integer createOpportunityData(Map<String,Object> data);
}
