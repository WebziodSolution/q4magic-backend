package com.q4magic.performance.service;

import com.q4magic.common.dto.PerformanceDto;

public interface PerformanceService {
    PerformanceDto getPerformanceByCustomerId(Integer cusId,String startDateTime ,String endDateTime);
}
