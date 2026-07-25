package com.q4magic.closePlan.service;

import com.q4magic.common.dto.ClosePlanDto;
import com.q4magic.common.dto.ClosePlanUrlResponseDto;

import java.util.List;
import java.util.Map;

public interface ClosePlanService {
    List<ClosePlanDto> getClosePlanByOppId(Integer oppId);

    List<ClosePlanDto> getClosePlanByOppIdAndStatus(Integer oppId);

    List<ClosePlanUrlResponseDto> saveAndGenerateUrl(Integer cusId, List<ClosePlanDto> closePlanDtoList);

    Map<String, Object> validateToken(String token);

    void changeStatus(Integer id);

}
