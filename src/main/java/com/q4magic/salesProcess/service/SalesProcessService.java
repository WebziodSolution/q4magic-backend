package com.q4magic.salesProcess.service;

import com.q4magic.common.dto.SalesProcessDto;

import java.util.List;

public interface SalesProcessService {
    List<SalesProcessDto> getAllByOpportunity(Integer opportunityId);

    SalesProcessDto getSaleProcess(Integer id);

    SalesProcessDto createSaleProcess(SalesProcessDto saleProcessDto);

    SalesProcessDto updateSaleProcess(Integer id, SalesProcessDto saleProcessDto);

    void deleteSaleProcess(Integer id);
}
