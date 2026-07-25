package com.q4magic.opportunitiesCurrentEnvironment.service;

import com.q4magic.common.dto.OpportunitiesCurrentEnvironmentDto;

import java.util.List;

public interface OpportunitiesCurrentEnvironmentService {
    List<OpportunitiesCurrentEnvironmentDto> getOpportunitiesCurrentEnvironmentByOppId(Integer id);

    OpportunitiesCurrentEnvironmentDto getOpportunitiesCurrentEnvironment(Integer id);

    OpportunitiesCurrentEnvironmentDto addOpportunitiesCurrentEnvironment(OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto);

    OpportunitiesCurrentEnvironmentDto updateOpportunitiesCurrentEnvironment(Integer id, OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto);

    void deleteOpportunitiesCurrentEnvironment(Integer id);
}
