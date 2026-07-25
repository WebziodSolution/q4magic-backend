package com.q4magic.salesStage.service;

import com.q4magic.common.dto.SalesStagesDto;

import java.util.List;

public interface SalesStageService {
    List<SalesStagesDto> getAllSalesStages();

    SalesStagesDto getSalesStageById(Integer id);

    SalesStagesDto createSalesStage(SalesStagesDto salesStagesDto);

    SalesStagesDto updateSalesStage(Integer id, SalesStagesDto salesStagesDto);

    void deleteSalesStage(Integer id);
}
