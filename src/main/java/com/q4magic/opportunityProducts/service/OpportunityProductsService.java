package com.q4magic.opportunityProducts.service;

import com.q4magic.common.dto.OpportunityProductsDto;

import java.util.List;

public interface OpportunityProductsService {
    List<OpportunityProductsDto> getAllOppProducts(Integer id);

    OpportunityProductsDto getOppProducts(Integer id);

    OpportunityProductsDto createOppProducts(OpportunityProductsDto opportunityProductsDto, Boolean isSyncToSalesforce,Integer createdById);

    OpportunityProductsDto updateOppProducts(Integer id, OpportunityProductsDto opportunityProductsDto, Boolean isSyncToSalesforce,Integer createdById);

    void deleteOppProducts(Integer id, Boolean isSyncToSalesforce,Integer createdById);
}
